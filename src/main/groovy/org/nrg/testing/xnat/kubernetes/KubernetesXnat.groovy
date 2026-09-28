package org.nrg.testing.xnat.kubernetes

import groovy.util.logging.Log4j
import org.nrg.testing.xnat.conf.Settings
import org.nrg.testing.xnat.conf.XNATProperties

import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * An XNAT deployed on Kubernetes as a workload (by default the XNAT Helm chart's StatefulSet) with its database in a
 * pod of the same namespace. Holds what the performance tests need to reset it, change its plugins and run another
 * image, all through kubectl.
 *
 * Resetting wipes the XNAT's data directories and recreates its database, so it refuses to run until
 * {@code xnat.k8s.reset.confirm} names this exact target, {@code <context>/<namespace>}.
 */
@Log4j
class KubernetesXnat {

    private static final List<String> DATA_PATH_PREFERENCES = ['archivePath', 'prearchivePath', 'cachePath', 'buildPath']
    private static final String IDENTIFIER = /[A-Za-z_][A-Za-z0-9_]*/

    final Kubectl kubectl
    String workload = 'statefulset/xnat'
    String pod = 'xnat-0'
    String container
    String databasePod
    String databaseContainer
    String databaseName = 'xnat'
    String databaseUser
    List<String> dataPaths
    String pluginsDirectory = '/data/xnat/home/plugins'
    String pluginsSource
    String imageRepository
    long startupTimeoutSeconds = 900
    String resetConfirmation
    File artifactCache = new File(System.getProperty('java.io.tmpdir'), 'xnat-kubernetes-artifacts')

    private String stagedImage

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
        xnat.pod = properties.kubernetesPod
        xnat.container = properties.kubernetesContainer
        xnat.databasePod = properties.kubernetesDatabasePod
        xnat.databaseContainer = properties.kubernetesDatabaseContainer
        xnat.databaseName = properties.kubernetesDatabaseName
        xnat.databaseUser = properties.kubernetesDatabaseUser
        xnat.dataPaths = properties.kubernetesDataPaths
        xnat.pluginsDirectory = properties.kubernetesPluginsDirectory
        xnat.pluginsSource = properties.kubernetesPluginsSource
        xnat.imageRepository = properties.kubernetesImage
        xnat.startupTimeoutSeconds = properties.kubernetesStartupTimeout
        xnat.resetConfirmation = properties.kubernetesResetConfirmation
        xnat
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
     * the workload to zero, recreates the database, then starts the workload on the staged image, if any.
     */
    void reset() {
        requireResetConfirmation()
        if (!databasePod) {
            throw new IllegalStateException("Set ${XNATProperties.KUBERNETES_DB_POD} to the pod that runs the XNAT's database")
        }
        wipeDataDirectories()
        stop()
        recreateDatabase()
        start()
    }

    void wipeDataDirectories() {
        final List<String> paths = resolveDataPaths()
        log.info("Wiping ${paths} in ${target} pod ${pod}")
        final String script = (['set -e'] + paths.collect { path ->
            "if [ -d '${path}' ]; then find '${path}' -mindepth 1 -maxdepth 1 -exec rm -rf {} +; fi"
        }).join('\n')
        kubectl.exec(pod, container, script, 3600)
    }

    void stop() {
        log.info("Stopping ${workload} in ${target}")
        kubectl.scale(workload, 0)
        kubectl.waitForPodDeletion(pod, startupTimeoutSeconds)
    }

    void recreateDatabase() {
        requireIdentifier(databaseName, XNATProperties.KUBERNETES_DB_NAME)
        final String psql = databaseUser ? "psql -U ${requireIdentifier(databaseUser, XNATProperties.KUBERNETES_DB_USER)}" : 'psql'
        log.info("Recreating database ${databaseName} in ${target} pod ${databasePod}")
        kubectl.exec(databasePod, databaseContainer, """\
            set -e
            owner=\$(${psql} -At -d postgres -c "SELECT pg_get_userbyid(datdba) FROM pg_database WHERE datname = '${databaseName}'")
            if [ -z "\$owner" ]; then owner='${databaseName}'; fi
            ${psql} -v ON_ERROR_STOP=1 -d postgres -c "DROP DATABASE IF EXISTS \\"${databaseName}\\" WITH (FORCE)"
            ${psql} -v ON_ERROR_STOP=1 -d postgres -c "CREATE DATABASE \\"${databaseName}\\" OWNER \\"\$owner\\""
            """.stripIndent())
    }

    /** Starts the workload on the staged image, if one is staged, and waits for the pod to be Ready. */
    void start() {
        if (stagedImage) {
            final String name = container ?: kubectl.firstContainerName(workload)
            log.info("Switching ${workload} container ${name} to ${stagedImage}")
            kubectl.setImage(workload, name, stagedImage)
            stagedImage = null
        }
        log.info("Starting ${workload} in ${target}")
        kubectl.scale(workload, 1)
        waitForReady()
    }

    void waitForReady() {
        kubectl.waitForPodReady(pod, startupTimeoutSeconds)
    }

    /** Stages the image for an XNAT version, to be used the next time the workload starts. */
    void stageImageForVersion(String xnatVersion) {
        if (!imageRepository) {
            throw new IllegalStateException("Set ${XNATProperties.KUBERNETES_IMAGE} to the image repository whose tags are XNAT versions")
        }
        stagedImage = "${imageRepository}:${xnatVersion}"
        log.info("Staged ${stagedImage} for the next start of ${workload}")
    }

    boolean hasStagedImage() {
        stagedImage != null
    }

    /** Restarts the workload onto a staged image. Nothing happens when no image is staged. */
    void applyStagedImage() {
        if (stagedImage) {
            stop()
            start()
        }
    }

    void installPlugin(String pluginName, File jar) {
        requireFileName(pluginName)
        Kubectl.requireSafePath(pluginsDirectory)
        kubectl.upload(jar, pod, container, "${pluginsDirectory}/${pluginName}")
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
        kubectl.exec(pod, container, "rm -f '${pluginsDirectory}/${pluginName}'")
    }

    void uninstallAllPlugins() {
        Kubectl.requireSafePath(pluginsDirectory)
        kubectl.exec(pod, container, "rm -f '${pluginsDirectory}'/*.jar")
    }

    List<String> resolveDataPaths() {
        final List<String> paths = dataPaths ?: readDataPathsFromSiteConfig()
        paths.each { path ->
            Kubectl.requireSafePath(path)
            if (path.split('/').findAll().size() < 2) {
                throw new IllegalArgumentException("Refusing to wipe ${path}: a data directory should be at least two levels deep")
            }
        }
        paths
    }

    private static List<String> readDataPathsFromSiteConfig() {
        final Map<String, Object> siteConfig = Settings.adminCredentials().get("${Settings.BASEURL}/xapi/siteConfig")
                .then().statusCode(200).extract().jsonPath().getMap('$')
        DATA_PATH_PREFERENCES.collect { preference ->
            final String path = siteConfig[preference] as String
            if (!path) {
                throw new IllegalStateException("The site configuration has no ${preference}; set ${XNATProperties.KUBERNETES_DATA_PATHS}")
            }
            path
        }
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
