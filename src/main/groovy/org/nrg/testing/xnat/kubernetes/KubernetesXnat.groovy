package org.nrg.testing.xnat.kubernetes

import groovy.json.JsonSlurper
import groovy.util.logging.Log4j
import org.nrg.testing.xnat.conf.Settings
import org.nrg.testing.xnat.conf.XNATProperties

import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.text.SimpleDateFormat
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

/**
 * An XNAT deployed on Kubernetes as a single-replica StatefulSet (by default the XNAT Helm chart's) with its database
 * in a pod of the same namespace. Holds what the performance tests need to reset it, change its plugins and run
 * another image, all through kubectl.
 *
 * Resetting wipes the XNAT's data directories and recreates its database, so it refuses to run until
 * {@code xnat.k8s.reset.confirm} names this exact target, {@code <context>/<namespace>}.
 */
@Log4j
class KubernetesXnat {

    private static final List<String> DATA_PATH_PREFERENCES = ['archivePath', 'prearchivePath', 'cachePath', 'buildPath']
    private static final List<String> EMPTY_AFTER_RESET_PREFERENCES = ['archivePath', 'prearchivePath']
    private static final List<String> EPHEMERAL_VOLUME_TYPES = ['emptyDir', 'ephemeral', 'configMap', 'secret', 'projected', 'downwardAPI']
    private static final List<String> STATEFUL_SET_KINDS = ['statefulset', 'statefulsets', 'sts', 'statefulset.apps', 'statefulsets.apps']
    private static final String IDENTIFIER = /[A-Za-z_][A-Za-z0-9_-]*/
    private static final long   LOG_SAVE_TIMEOUT_SECONDS = 600

    final Kubectl kubectl
    String workload = 'statefulset/xnat'
    String pod = 'xnat-0'
    String container
    List<String> containerNames = ['xnat', 'home-init']
    String databasePod
    String databaseContainer
    String databaseName = 'xnat'
    String databaseUser
    List<String> dataPaths
    String pluginsDirectory = '/data/xnat/home/plugins'
    String pluginsSource
    String imageRepository
    String logsDirectory = '/data/xnat/home/logs'
    /** Where the XNAT's logs are saved before each stop; null saves nothing. */
    File savedLogs
    long startupTimeoutSeconds = 900
    /** How long a started XNAT's DICOM receiver gets to accept connections, each time it is waited for. It is usually open within seconds of the REST API. */
    long dicomReceiverWaitMillis = 30000
    boolean verifyVersion = true
    String resetConfirmation
    File artifactCache = new File(System.getProperty('java.io.tmpdir'), 'xnat-kubernetes-artifacts')
    /** Reads the XNAT's site configuration. */
    Closure<Map<String, Object>> siteConfigReader = {
        Settings.adminCredentials().get("${Settings.BASEURL}/xapi/siteConfig").then().statusCode(200).extract().jsonPath().getMap('$')
    }
    /** Reads the ids of the plugins the running XNAT loaded. */
    Closure<Collection<String>> loadedPluginsReader = {
        Settings.adminCredentials().get("${Settings.BASEURL}/xapi/plugins").then().statusCode(200).extract().jsonPath().getMap('$').keySet()
    }

    private String stagedImage
    private String runningImage
    private Map<String, String> knownSiteDataPaths
    private boolean pluginsVolumeChecked
    private String knownEphemeralPluginsVolume
    private boolean warnedOfEphemeralPlugins
    private boolean describedFirstSwitch
    private final Map<String, Set<String>> installedPluginIds = [:]

    KubernetesXnat(Kubectl kubectl) {
        this.kubectl = kubectl
    }

    /** The configured deployment, or null when {@code xnat.k8s.namespace} is not set. */
    static KubernetesXnat fromProperties(XNATProperties properties) {
        final String namespace = properties.kubernetesNamespace
        if (!namespace) {
            return null
        }
        final KubernetesXnat xnat = new KubernetesXnat(new Kubectl(properties.kubernetesKubectl, properties.kubernetesContext, namespace))
        xnat.workload = properties.kubernetesWorkload
        xnat.pod = properties.kubernetesPod ?: podOf(xnat.workload)
        xnat.container = properties.kubernetesContainer
        xnat.containerNames = properties.kubernetesContainers
        xnat.databasePod = properties.kubernetesDatabasePod
        xnat.databaseContainer = properties.kubernetesDatabaseContainer
        xnat.databaseName = properties.kubernetesDatabaseName
        xnat.databaseUser = properties.kubernetesDatabaseUser
        xnat.dataPaths = properties.kubernetesDataPaths
        xnat.pluginsDirectory = properties.kubernetesPluginsDirectory
        xnat.pluginsSource = properties.kubernetesPluginsSource
        xnat.imageRepository = properties.kubernetesImage
        xnat.logsDirectory = properties.kubernetesLogsDirectory
        xnat.savedLogs = new File(properties.kubernetesSaveLogsTo)
        xnat.startupTimeoutSeconds = properties.kubernetesStartupTimeout
        xnat.resetConfirmation = properties.kubernetesResetConfirmation
        xnat.verifyVersion = properties.kubernetesVerifyVersion
        xnat
    }

    /** The pod of a single-replica StatefulSet: {@code statefulset/xnat} runs {@code xnat-0}. */
    static String podOf(String workload) {
        final List<String> parts = workload.split('/') as List<String>
        if (parts.size() != 2 || !(parts[0].toLowerCase() in STATEFUL_SET_KINDS)) {
            throw new IllegalArgumentException("${XNATProperties.KUBERNETES_WORKLOAD} must be a StatefulSet, such as statefulset/xnat, not ${workload}; " +
                    "set ${XNATProperties.KUBERNETES_POD} if its pod is not <name>-0")
        }
        "${parts[1]}-0"
    }

    String getTarget() {
        kubectl.target
    }

    void requireResetConfirmation() {
        if (resetConfirmation != target) {
            throw new IllegalStateException("Refusing to wipe the XNAT at ${target}: resetting deletes its data and recreates its database. " +
                    "Set ${XNATProperties.KUBERNETES_RESET_CONFIRM}=${target} to confirm that this XNAT holds only test data.")
        }
    }

    /**
     * Returns the XNAT to an empty archive and a fresh database: wipes the data directories while the pod is up, scales
     * the workload to zero, recreates the database, starts the workload on the staged image, if any, and checks that
     * the archive and prearchive came back empty. Everything that can be checked is checked before the first change,
     * so a mistake in the configuration leaves the XNAT as it was.
     */
    void reset() {
        requireResetConfirmation()
        if (!databasePod) {
            throw new IllegalStateException("Set ${XNATProperties.KUBERNETES_DB_POD} to the pod that runs the XNAT's database")
        }
        final String psql = psqlCommand()
        final List<String> paths = resolveDataPaths()
        final List<String> mustBeEmpty = pathsEmptyAfterReset()
        final List<String> containers = stagedImage ? xnatContainerNames() : null
        wipe(paths)
        stop()
        recreateDatabaseWith(psql)
        startSwitching(containers)
        requireEmpty(mustBeEmpty)
    }

    void wipeDataDirectories() {
        wipe(resolveDataPaths())
    }

    private void wipe(List<String> paths) {
        log.info("Wiping ${paths} in ${target} pod ${pod}")
        final String script = (['set -e'] + paths.collect { path ->
            "if [ -d '${path}' ]; then find '${path}' -mindepth 1 -maxdepth 1 -exec rm -rf {} +; fi"
        }).join('\n')
        kubectl.exec(pod, container, script, 3600)
    }

    /**
     * Fails when a directory holds anything after the restart. The wipe runs while XNAT is up, so a file XNAT wrote
     * after it, or held open on NFS, which keeps a deleted open file as {@code .nfsXXXX}, can survive it.
     */
    private void requireEmpty(List<String> paths) {
        final String leftovers = kubectl.exec(pod, container, "for d in ${paths.collect { path -> "'${path}'" }.join(' ')}; do " +
                'if [ -d "$d" ]; then find "$d" -mindepth 1 -maxdepth 1; fi; done | head -n 5').trim()
        if (leftovers) {
            throw new IllegalStateException("The reset left files in ${paths}, such as:\n${leftovers}\n" +
                    'The wipe runs while XNAT is still up, so these survived it. Empty the directories and run the test again.')
        }
    }

    void stop() {
        saveLogs()
        log.info("Stopping ${workload} in ${target}")
        kubectl.scale(workload, 0)
        kubectl.waitForPodDeletion(pod, startupTimeoutSeconds)
    }

    /**
     * Saves the container's log and a tarball of XNAT's logs directory into {@link #savedLogs}, since the XNAT Helm
     * chart keeps XNAT's home, logs included, on a volume that goes with the pod. A log that can't be saved only gets
     * a warning.
     */
    void saveLogs() {
        if (savedLogs == null) {
            return
        }
        savedLogs.mkdirs()
        final String prefix = "${timestamp()}-${pod}"
        saveLog(new File(savedLogs, "${prefix}.log")) { File file ->
            kubectl.logsToFile(pod, container, file, LOG_SAVE_TIMEOUT_SECONDS)
        }
        saveLog(new File(savedLogs, "${prefix}-logs.tar.gz")) { File file ->
            Kubectl.requireSafePath(logsDirectory)
            kubectl.execToFile(pod, container, "tar -czf - -C '${logsDirectory}' .", file, LOG_SAVE_TIMEOUT_SECONDS)
        }
    }

    private static void saveLog(File file, Closure save) {
        try {
            save(file)
            log.info("Saved ${file}")
        } catch (Exception e) {
            file.delete()
            log.warn("Could not save ${file.name}: ${e.message}")
        }
    }

    void recreateDatabase() {
        recreateDatabaseWith(psqlCommand())
    }

    private void recreateDatabaseWith(String psql) {
        log.info("Recreating database ${databaseName} in ${target} pod ${databasePod}")
        kubectl.exec(databasePod, databaseContainer, """\
            set -e
            owner=\$(${psql} -At -d postgres -c "SELECT pg_get_userbyid(datdba) FROM pg_database WHERE datname = '${databaseName}'")
            if [ -z "\$owner" ]; then owner='${databaseName}'; fi
            ${psql} -v ON_ERROR_STOP=1 -d postgres -c "DROP DATABASE IF EXISTS \\"${databaseName}\\" WITH (FORCE)"
            ${psql} -v ON_ERROR_STOP=1 -d postgres -c "CREATE DATABASE \\"${databaseName}\\" OWNER \\"\$owner\\""
            """.stripIndent())
    }

    /** The psql command for the database container, once the database and user names are known to be safe to use. */
    private String psqlCommand() {
        requireIdentifier(databaseName, XNATProperties.KUBERNETES_DB_NAME)
        databaseUser ? "psql -U ${requireIdentifier(databaseUser, XNATProperties.KUBERNETES_DB_USER)}" : 'psql'
    }

    /**
     * Starts the workload on the staged image, if one is staged, and waits for the pod to be Ready. The image goes to
     * every container and init container named in {@code xnat.k8s.containers}, not only the main one: the XNAT Helm
     * chart's home-init init container, for one, copies Tomcat and the XNAT webapp out of its image into the volumes
     * the main container runs from.
     */
    void start() {
        startSwitching(stagedImage ? xnatContainerNames() : null)
    }

    /** Starts the workload, first switching these containers to the staged image, if one is staged. */
    private void startSwitching(List<String> containers) {
        if (stagedImage) {
            log.info("Switching ${workload} containers ${containers} to ${stagedImage}")
            kubectl.setImages(workload, containers, stagedImage)
            runningImage = stagedImage
            stagedImage = null
        }
        log.info("Starting ${workload} in ${target}")
        kubectl.scale(workload, 1)
        waitForReady()
        logRunningImages()
    }

    void waitForReady() {
        kubectl.waitForPodReady(pod, startupTimeoutSeconds)
    }

    /** Logs the image each container of the pod runs by its digest, which names the exact build even when a tag moves. */
    void logRunningImages() {
        try {
            final Map status = (parseJson(kubectl.run(['get', 'pod', pod, '-o', 'json'])).status ?: [:]) as Map
            final List<Map> statuses = ((status.initContainerStatuses ?: []) + (status.containerStatuses ?: [])) as List<Map>
            log.info("${pod} runs ${statuses.collect { entry -> "${entry.name}=${entry.imageID ?: entry.image}" }.join(', ') ?: 'no containers yet'}")
        } catch (Exception e) {
            log.warn("Could not read the images ${pod} runs: ${e.message}")
        }
    }

    /**
     * Waits for XNAT's DICOM receiver to accept connections, restarting XNAT once if it doesn't. XNAT starts its
     * receivers after its REST API answers, so a C-STORE sent right after a restart can be refused; and a start whose
     * receiver never opened has been seen to come up normally when restarted. A receiver that is still closed after the
     * restart, such as a disabled one, only gets a warning, so tests that send no DICOM don't fail on it.
     */
    void waitForDicomReceiver(String host, int port) {
        if (waitForPort(host, port, dicomReceiverWaitMillis)) {
            return
        }
        log.warn("XNAT's DICOM receiver at ${host}:${port} did not accept connections within ${dicomReceiverWaitMillis / 1000} s; restarting ${workload} once")
        stop()
        start()
        if (!waitForPort(host, port, dicomReceiverWaitMillis)) {
            log.warn("XNAT's DICOM receiver at ${host}:${port} still did not accept connections within ${dicomReceiverWaitMillis / 1000} s of the restart")
        }
    }

    /** Whether something accepts connections on host:port within the timeout, trying once a second. */
    static boolean waitForPort(String host, int port, long timeoutMillis) {
        final long deadline = System.currentTimeMillis() + timeoutMillis
        while (true) {
            final Socket socket = new Socket()
            try {
                socket.connect(new InetSocketAddress(host, port), 2000)
                return true
            } catch (IOException ignored) {
                if (System.currentTimeMillis() >= deadline) {
                    return false
                }
            } finally {
                socket.close()
            }
            Thread.sleep(1000)
        }
    }

    /**
     * Stages an image for the next start of the workload: a full reference, such as
     * {@code ghcr.io/nrgxnat/xnat:1.10.1}, or a tag of {@code xnat.k8s.image}.
     */
    void stageImage(String image) {
        if (image ==~ /.*[\/:@].*/) {
            stagedImage = image
        } else if (imageRepository) {
            stagedImage = "${imageRepository}:${image}"
        } else {
            throw new IllegalStateException("Set ${XNATProperties.KUBERNETES_IMAGE} to the image repository whose tag ${image} is, or give the deployment a full image reference")
        }
        log.info("Staged ${stagedImage} for the next start of ${workload}")
    }

    boolean hasStagedImage() {
        stagedImage != null
    }

    /** Restarts the workload onto a staged image. Nothing happens when no image is staged. */
    void applyStagedImage() {
        if (stagedImage) {
            final List<String> containers = xnatContainerNames()
            stop()
            startSwitching(containers)
        }
    }

    /** The tag of an image reference: null when a digest pins it, {@code latest} when it names neither. */
    static String tagOf(String image) {
        if (image.contains('@')) {
            return null
        }
        final int colon = image.lastIndexOf(':')
        colon > image.lastIndexOf('/') ? image.substring(colon + 1) : 'latest'
    }

    /**
     * After a switch of image, checks that XNAT itself reports the version the image's tag names, so that a run can
     * never measure one build under another's label. {@code xnat.k8s.verifyVersion=false} turns it off for images whose
     * version differs from their tag; an image pinned by digest has no tag to check.
     */
    void verifyRunningVersion() {
        if (!verifyVersion || !runningImage) {
            return
        }
        final String tag = tagOf(runningImage)
        if (tag == null) {
            log.info("Not checking XNAT's version: ${runningImage} has no tag")
            return
        }
        final String version = Settings.adminCredentials().get("${Settings.BASEURL}/xapi/siteConfig/buildInfo")
                .then().statusCode(200).extract().jsonPath().getString('version')
        if (version != tag) {
            throw new IllegalStateException("XNAT reports version ${version} after ${workload} was switched to ${runningImage}. " +
                    "Check that every container supplying XNAT runs the new image, or set ${XNATProperties.KUBERNETES_VERIFY_VERSION}=false " +
                    "if this image's version differs from its tag.")
        }
        log.info("XNAT reports version ${version}, as its image tag says")
    }

    /**
     * The containers and init containers of the workload's pod template named in {@code xnat.k8s.containers}, which a
     * switch of image changes. At least one must be a main container. The first time, logs what they run, since the
     * workload stays on the run's last image afterwards.
     */
    List<String> xnatContainerNames() {
        final Map podSpec = podTemplateSpec()
        final List<Map> mainContainers = (podSpec.containers ?: []) as List<Map>
        final List<Map> initContainers = (podSpec.initContainers ?: []) as List<Map>
        final List<Map> named = (initContainers + mainContainers).findAll { entry -> entry.name in containerNames }
        if (!mainContainers.any { entry -> entry in named }) {
            throw new IllegalStateException("None of the containers of ${workload}, ${mainContainers*.name}, is named in ${containerNames}; " +
                    "set ${XNATProperties.KUBERNETES_CONTAINERS} to the containers and init containers that run the XNAT image")
        }
        final List<String> names = named*.name as List<String>
        final List<String> absent = containerNames - names
        if (absent) {
            log.warn("${workload} has no container or init container ${absent}; switching only ${names}")
        }
        if (!describedFirstSwitch) {
            describedFirstSwitch = true
            log.warn("Before this run's first switch of image, ${workload} runs ${named.collect { entry -> "${entry.name}=${entry.image}" }.join(', ')}. " +
                    "It stays on the last image the run switches to, with a database that image's XNAT set up, which an older XNAT may not " +
                    'start on. To leave it on the image it runs now, make that image the last deployment of the run.')
        }
        names
    }

    void installPlugin(String pluginName, File jar) {
        requireFileName(pluginName)
        Kubectl.requireSafePath(pluginsDirectory)
        final String ephemeralVolume = ephemeralPluginsVolume()
        if (ephemeralVolume) {
            throw new IllegalStateException("Refusing to install ${pluginName}: ${pluginsDirectory} is on ${ephemeralVolume}, which the pod loses when the reset " +
                    "restarts it, so XNAT would start without the plugin. Give ${workload} a persistent plugins directory, or install the plugin " +
                    "through the XNAT Helm chart's plugins values and leave it out of the run's deployments and tests.")
        }
        final Set<String> ids = pluginIdsIn(jar)
        kubectl.upload(jar, pod, container, "${pluginsDirectory}/${pluginName}")
        installedPluginIds[pluginName] = ids
    }

    /** Installs a plugin from {@code xnat.k8s.pluginsSource}, a local directory or an http(s) URL prefix. */
    void installPlugin(String pluginName) {
        if (!pluginsSource) {
            throw new IllegalStateException("Set ${XNATProperties.KUBERNETES_PLUGINS_SOURCE} to the directory or URL that holds ${pluginName}")
        }
        installPlugin(pluginName, pluginsSource ==~ /^https?:.*/ ?
                download(pluginName, "${pluginsSource.replaceAll('/+$', '')}/${pluginName}") :
                new File(pluginsSource, pluginName))
    }

    void installPluginFromUrl(String pluginName, String url) {
        installPlugin(pluginName, download(pluginName, url))
    }

    void uninstallPlugin(String pluginName) {
        requireFileName(pluginName)
        Kubectl.requireSafePath(pluginsDirectory)
        installedPluginIds.remove(pluginName)
        if (!pluginsDirectoryIsEphemeral()) {
            kubectl.exec(pod, container, "rm -f '${pluginsDirectory}/${pluginName}'")
        }
    }

    void uninstallAllPlugins() {
        Kubectl.requireSafePath(pluginsDirectory)
        installedPluginIds.clear()
        if (!pluginsDirectoryIsEphemeral()) {
            kubectl.exec(pod, container, "rm -f '${pluginsDirectory}'/*.jar")
        }
    }

    /**
     * After a restart, checks that XNAT loaded every plugin installed since the plugins were last cleared, so that a
     * test can never measure an XNAT without a plugin it asked for. A jar that declares no plugin id can't be checked.
     */
    void verifyInstalledPlugins() {
        if (!installedPluginIds) {
            return
        }
        final Collection<String> loaded = loadedPluginsReader.call()
        installedPluginIds.findAll { name, ids -> !ids }.each { name, ids ->
            log.warn("${name} declares no XNAT plugin id, so there is nothing to check it by")
        }
        final Map<String, Set<String>> missing = installedPluginIds.collectEntries { name, ids -> [(name): ids - loaded] }.findAll { name, ids -> ids }
        if (missing) {
            throw new IllegalStateException("XNAT did not load ${missing.collect { name, ids -> "${ids.join(', ')} from ${name}" }.join('; ')}. " +
                    "The plugins it loaded are ${loaded.sort()}.")
        }
        log.info("XNAT loaded every plugin installed for this test: ${installedPluginIds.keySet().sort()}")
    }

    /** The ids of the XNAT plugins a jar holds, from its {@code META-INF/xnat/**}{@code /*-plugin.properties} files. */
    static Set<String> pluginIdsIn(File jar) {
        final ZipFile zip = new ZipFile(jar)
        try {
            final List<ZipEntry> descriptors = Collections.list(zip.entries()).findAll { ZipEntry entry ->
                entry.name.startsWith('META-INF/xnat/') && entry.name.endsWith('-plugin.properties')
            }
            descriptors.collect { ZipEntry entry ->
                final Properties properties = new Properties()
                zip.getInputStream(entry).withStream { stream -> properties.load(stream) }
                properties.getProperty('id')
            }.findAll() as Set<String>
        } finally {
            zip.close()
        }
    }

    /**
     * What the plugins directory is on, when the pod loses it at a restart, such as the emptyDir the XNAT Helm chart
     * mounts there; null when it is on a persistent volume. Read from the workload's pod template once.
     */
    String ephemeralPluginsVolume() {
        if (!pluginsVolumeChecked) {
            knownEphemeralPluginsVolume = findEphemeralPluginsVolume(podTemplateSpec())
            pluginsVolumeChecked = true
        }
        knownEphemeralPluginsVolume
    }

    private String findEphemeralPluginsVolume(Map podSpec) {
        final List<Map> containers = (podSpec.containers ?: []) as List<Map>
        final Map main = container ? containers.find { entry -> entry.name == container } :
                (containers.find { entry -> entry.name in containerNames } ?: containers[0])
        if (main == null) {
            throw new IllegalStateException("${workload} has no container ${container}")
        }
        final Map mount = ((main.volumeMounts ?: []) as List<Map>).findAll { entry ->
            final String mountPath = (entry.mountPath as String).replaceAll('/+$', '')
            pluginsDirectory == mountPath || pluginsDirectory.startsWith("${mountPath}/")
        }.max { entry -> (entry.mountPath as String).length() }
        if (mount == null) {
            return "the ${main.name} container's own filesystem"
        }
        // A mount with no volume in the pod template is a StatefulSet's volume claim template: persistent.
        final Map volume = ((podSpec.volumes ?: []) as List<Map>).find { entry -> entry.name == mount.name }
        final String type = volume == null ? null : EPHEMERAL_VOLUME_TYPES.find { candidate -> volume.containsKey(candidate) }
        type ? "the ${type} volume ${mount.name}" : null
    }

    /**
     * Whether the plugins directory is emptied at every restart, so that removing a plugin from it does nothing the
     * restart wouldn't; whatever the workload puts there as it starts, such as the XNAT Helm chart's plugins, stays.
     */
    private boolean pluginsDirectoryIsEphemeral() {
        final String ephemeralVolume = ephemeralPluginsVolume()
        if (ephemeralVolume && !warnedOfEphemeralPlugins) {
            warnedOfEphemeralPlugins = true
            log.warn("Not removing plugins from ${pluginsDirectory}: it is on ${ephemeralVolume}, which every restart empties. " +
                    "Plugins ${workload} installs there as it starts, such as the XNAT Helm chart's, stay installed.")
        }
        ephemeralVolume != null
    }

    /** The directories the reset empties: {@code xnat.k8s.dataPaths}, or the site configuration's archive, prearchive, cache and build paths. */
    List<String> resolveDataPaths() {
        final List<String> paths = dataPaths ?: siteDataPaths(DATA_PATH_PREFERENCES)
        paths.each { path ->
            Kubectl.requireSafePath(path)
            if (path.split('/').findAll().size() < 2) {
                throw new IllegalArgumentException("Refusing to wipe ${path}: a data directory should be at least two levels deep")
            }
        }
        paths
    }

    /** The directories that must be empty after a reset: {@code xnat.k8s.dataPaths}, or the archive and prearchive. */
    List<String> pathsEmptyAfterReset() {
        dataPaths ?: siteDataPaths(EMPTY_AFTER_RESET_PREFERENCES)
    }

    /**
     * Data paths from the site configuration, which is read at the first reset and kept, so that a later reset
     * doesn't depend on the state a failed test left XNAT in.
     */
    private List<String> siteDataPaths(List<String> preferences) {
        if (knownSiteDataPaths == null) {
            final Map<String, Object> siteConfig = siteConfigReader.call()
            final Map<String, String> paths = [:]
            DATA_PATH_PREFERENCES.each { preference ->
                final String path = siteConfig[preference] as String
                if (!path) {
                    throw new IllegalStateException("The site configuration has no ${preference}; set ${XNATProperties.KUBERNETES_DATA_PATHS}")
                }
                paths[preference] = path
            }
            knownSiteDataPaths = paths
        }
        final Map<String, String> known = knownSiteDataPaths
        preferences.collect { preference -> known[preference] }
    }

    private Map podTemplateSpec() {
        ((parseJson(kubectl.run(['get', workload, '-o', 'json'])).spec as Map).template as Map).spec as Map
    }

    private File download(String name, String url) {
        final File target = new File(artifactCache, name)
        if (!target.exists()) {
            artifactCache.mkdirs()
            log.info("Downloading ${url}")
            final File partial = new File(artifactCache, "${name}.part")
            new URL(url).withInputStream { stream -> Files.copy(stream, partial.toPath(), StandardCopyOption.REPLACE_EXISTING) }
            Files.move(partial.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
        target
    }

    private static Map parseJson(String json) {
        new JsonSlurper().parseText(json) as Map
    }

    private static String timestamp() {
        final SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'")
        format.timeZone = TimeZone.getTimeZone('UTC')
        format.format(new Date())
    }

    private static String requireIdentifier(String value, String property) {
        if (!(value ==~ IDENTIFIER)) {
            throw new IllegalArgumentException("${property} must be a plain SQL identifier, not ${value}")
        }
        value
    }

    private static void requireFileName(String name) {
        if (!(name ==~ /[A-Za-z0-9._+-]+/)) {
            throw new IllegalArgumentException("Refusing unsafe plugin file name: ${name}")
        }
    }

}
