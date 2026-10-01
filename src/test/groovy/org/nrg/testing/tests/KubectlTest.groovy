package org.nrg.testing.tests

import org.nrg.testing.xnat.kubernetes.Kubectl
import org.nrg.testing.xnat.kubernetes.KubectlException
import org.nrg.testing.xnat.kubernetes.KubernetesXnat
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

import static org.testng.AssertJUnit.assertEquals
import static org.testng.AssertJUnit.assertFalse
import static org.testng.AssertJUnit.assertTrue
import static org.testng.AssertJUnit.fail

/**
 * Runs {@link Kubectl} and {@link KubernetesXnat} against a stub kubectl that records what it was asked to do. The stub
 * prints {@code json} for a call with {@code -o json} and {@code stdout} for any other; {@code json-for-<argument>},
 * {@code stdout-for-<argument>} and {@code status-for-<argument>}, with any / in the argument written as _, answer only
 * the calls that have that argument.
 */
class KubectlTest {

    private File stubDir
    private File stub

    @BeforeMethod
    void createStub() {
        stubDir = Files.createTempDirectory('kubectl-stub').toFile()
        stub = new File(stubDir, 'kubectl')
        stub.text = '''\
            #!/bin/sh
            d=$(dirname "$0")
            printf '%s\\n' "$@" > "$d/args"
            printf '%s' "$*" | tr '\\n' ' ' >> "$d/calls"; echo >> "$d/calls"
            case " $* " in *" -i "*) cat > "$d/stdin";; esac
            [ -f "$d/sleep" ] && sleep "$(cat "$d/sleep")"
            out="$d/stdout"
            case " $* " in *" -o json "*) out="$d/json";; esac
            status="$d/status"
            for a in "$@"; do
              key=$(printf '%s' "$a" | tr '/' '_')
              [ -f "$out-for-$key" ] && out="$out-for-$key"
              [ -f "$d/status-for-$key" ] && status="$d/status-for-$key"
            done
            [ -f "$out" ] && cat "$out"
            [ -f "$d/stderr" ] && cat "$d/stderr" >&2
            exit "$(cat "$status" 2>/dev/null || echo 0)"
            '''.stripIndent()
        assertTrue(stub.setExecutable(true))
    }

    private Kubectl kubectl() {
        new Kubectl(stub.path, 'test-context', 'test-namespace')
    }

    private List<String> recordedArguments() {
        new File(stubDir, 'args').readLines()
    }

    /** Every call so far, one line each. */
    private List<String> calls() {
        final File calls = new File(stubDir, 'calls')
        calls.exists() ? calls.readLines() : []
    }

    /** Makes the stub answer calls with the argument with this output instead. */
    private void answer(String argument, String output) {
        new File(stubDir, "stdout-for-${argument.replace('/', '_')}").text = output
    }

    private void answerJson(String argument, String json) {
        new File(stubDir, "json-for-${argument.replace('/', '_')}").text = json
    }

    /** An XNAT whose reset is confirmed and fully configured, which answers that its pod exists. */
    private KubernetesXnat resettableXnat() {
        final KubernetesXnat xnat = new KubernetesXnat(kubectl())
        xnat.resetConfirmation = 'test-context/test-namespace'
        xnat.databasePod = 'xnat-postgres-1'
        xnat.dataPaths = ['/data/xnat/archive', '/data/xnat/prearchive']
        answer('pods', 'pod/xnat-0')
        xnat
    }

    /** The script of the last exec: everything after {@code -- sh -c}, rejoined across the lines it spans. */
    private String recordedScript() {
        final List<String> arguments = recordedArguments()
        final int separator = arguments.indexOf('--')
        assertEquals(['--', 'sh', '-c'], arguments[separator..separator + 2])
        arguments[separator + 3..-1].join('\n')
    }

    @Test
    void commandNamesTheContextAndNamespace() {
        assertEquals(['kubectl', '--context', 'ctx', '--namespace', 'ns', 'get', 'pods'],
                new Kubectl(null, 'ctx', 'ns').command(['get', 'pods']))
    }

    @Test
    void commandLeavesTheContextToKubectlWhenUnset() {
        final Kubectl kubectl = new Kubectl(null, '', 'ns')
        assertEquals(['kubectl', '--namespace', 'ns', 'get', 'pods'], kubectl.command(['get', 'pods']))
        assertEquals('in-cluster/ns', kubectl.target)
    }

    @Test
    void execRunsTheScriptInTheNamedContainer() {
        new File(stubDir, 'stdout').text = 'hello'
        assertEquals('hello', kubectl().exec('xnat-0', 'xnat', 'echo hello'))
        assertEquals(['--context', 'test-context', '--namespace', 'test-namespace', 'exec', 'xnat-0', '-c', 'xnat', '--', 'sh', '-c', 'echo hello'],
                recordedArguments())
    }

    @Test
    void execUsesTheDefaultContainerWhenNoneIsNamed() {
        kubectl().exec('xnat-0', null, 'true')
        assertFalse(recordedArguments().takeWhile { it != '--' }.contains('-c'))
    }

    @Test
    void aFailingCommandReportsItsExitStatusAndStandardError() {
        new File(stubDir, 'status').text = '3'
        new File(stubDir, 'stderr').text = 'pods "xnat-0" not found'
        try {
            kubectl().run(['get', 'pod', 'xnat-0'])
            fail('a non-zero exit should raise')
        } catch (KubectlException e) {
            assertEquals(3, e.exitStatus)
            assertTrue(e.message.contains('pods "xnat-0" not found'))
        }
    }

    @Test
    void aCommandThatOutlivesItsTimeoutIsStopped() {
        new File(stubDir, 'sleep').text = '10'
        final long started = System.currentTimeMillis()
        try {
            kubectl().run(['get', 'pods'], 1)
            fail('a command past its timeout should raise')
        } catch (KubectlException e) {
            assertTrue(e.message.contains('timed out'))
        }
        assertTrue(System.currentTimeMillis() - started < 8000)
    }

    @Test
    void uploadStreamsTheFileInThroughAStagingName() {
        final File jar = new File(stubDir, 'plugin.jar')
        jar.bytes = [1, 2, 3, 4] as byte[]
        kubectl().upload(jar, 'xnat-0', null, '/data/xnat/home/plugins/plugin.jar')
        assertEquals([1, 2, 3, 4] as byte[], new File(stubDir, 'stdin').bytes)
        assertEquals("cat > '/data/xnat/home/plugins/plugin.jar.uploading' && mv '/data/xnat/home/plugins/plugin.jar.uploading' '/data/xnat/home/plugins/plugin.jar'",
                recordedArguments().last())
        assertTrue(recordedArguments().contains('-i'))
    }

    @Test
    void unsafeContainerPathsAreRefused() {
        ['relative/path', "/data/it's", '/data/../etc', null].each { path ->
            try {
                Kubectl.requireSafePath(path)
                fail("${path} should be refused")
            } catch (IllegalArgumentException ignored) {}
        }
        Kubectl.requireSafePath('/data/xnat/archive')
    }

    @Test
    void resetRefusesATargetItWasNotConfirmedFor() {
        final KubernetesXnat xnat = new KubernetesXnat(kubectl())
        xnat.databasePod = 'xnat-postgres-1'
        xnat.dataPaths = ['/data/xnat/archive']
        ['test-context/other-namespace', 'test-namespace', null].each { confirmation ->
            xnat.resetConfirmation = confirmation
            try {
                xnat.reset()
                fail("a reset confirmed as ${confirmation} should be refused")
            } catch (IllegalStateException e) {
                assertTrue(e.message.contains('xnat.k8s.reset.confirm=test-context/test-namespace'))
            }
        }
        assertFalse('kubectl must not run before the target is confirmed', new File(stubDir, 'args').exists())
    }

    @Test
    void wipingEmptiesEachDataDirectoryInOneScript() {
        final KubernetesXnat xnat = new KubernetesXnat(kubectl())
        xnat.dataPaths = ['/data/xnat/archive', '/data/xnat/prearchive']
        xnat.wipeDataDirectories()
        final String script = recordedScript()
        assertTrue(script.startsWith('set -e'))
        assertTrue(script.contains("find '/data/xnat/archive' -mindepth 1 -maxdepth 1 -exec rm -rf {} +"))
        assertTrue(script.contains("find '/data/xnat/prearchive' -mindepth 1 -maxdepth 1 -exec rm -rf {} +"))
    }

    @Test
    void shallowDataDirectoriesAreRefused() {
        final KubernetesXnat xnat = new KubernetesXnat(kubectl())
        ['/data', '/'].each { path ->
            xnat.dataPaths = [path]
            try {
                xnat.wipeDataDirectories()
                fail("wiping ${path} should be refused")
            } catch (IllegalArgumentException ignored) {}
        }
        assertFalse(new File(stubDir, 'args').exists())
    }

    @Test
    void theDatabaseNameMustBeAPlainIdentifier() {
        final KubernetesXnat xnat = new KubernetesXnat(kubectl())
        xnat.databasePod = 'xnat-postgres-1'
        xnat.databaseName = 'xnat"; DROP ROLE admin; --'
        try {
            xnat.recreateDatabase()
            fail('a database name that is not an identifier should be refused')
        } catch (IllegalArgumentException ignored) {}
        assertFalse(new File(stubDir, 'args').exists())
    }

    @Test
    void recreatingTheDatabaseKeepsItsOwner() {
        final KubernetesXnat xnat = new KubernetesXnat(kubectl())
        xnat.databasePod = 'xnat-postgres-1'
        xnat.databaseContainer = 'postgres'
        xnat.recreateDatabase()
        final List<String> arguments = recordedArguments()
        assertEquals(['exec', 'xnat-postgres-1', '-c', 'postgres', '--', 'sh', '-c'], arguments[4..10])
        final String script = recordedScript()
        assertTrue(script.contains("SELECT pg_get_userbyid(datdba) FROM pg_database WHERE datname = 'xnat'"))
        assertTrue(script.contains('DROP DATABASE IF EXISTS \\"xnat\\" WITH (FORCE)'))
        assertTrue(script.contains('CREATE DATABASE \\"xnat\\" OWNER \\"$owner\\"'))
    }

    @Test
    void theChartsDatabaseNameIsAnIdentifier() {
        final KubernetesXnat xnat = new KubernetesXnat(kubectl())
        xnat.databasePod = 'xnat-postgres-1'
        xnat.databaseName = 'xnat-web'
        xnat.databaseUser = 'xnat-web'
        xnat.recreateDatabase()
        final String script = recordedScript()
        assertTrue(script.contains('psql -U xnat-web -v ON_ERROR_STOP=1 -d postgres -c "DROP DATABASE IF EXISTS \\"xnat-web\\" WITH (FORCE)"'))
        assertTrue(script.contains("then owner='xnat-web'; fi"))
    }

    @Test
    void aResetChecksItsConfigurationBeforeChangingAnything() {
        final KubernetesXnat xnat = resettableXnat()
        [['xnat"; DROP ROLE admin; --', null], ['xnat', 'postgres; rm -rf /']].each { names ->
            xnat.databaseName = names[0]
            xnat.databaseUser = names[1]
            try {
                xnat.reset()
                fail("a reset with database ${names[0]} and user ${names[1]} should be refused")
            } catch (IllegalArgumentException ignored) {}
        }
        xnat.databaseName = 'xnat'
        xnat.databaseUser = null
        xnat.dataPaths = ['/data']
        try {
            xnat.reset()
            fail('a reset that would wipe a shallow directory should be refused')
        } catch (IllegalArgumentException ignored) {}
        assertEquals('no kubectl call before the configuration is checked', [], calls())
    }

    @Test
    void aResetWhoseImageSwitchWouldChangeNoContainerOnlyReadsTheWorkload() {
        final KubernetesXnat xnat = resettableXnat()
        xnat.containerNames = ['web']
        new File(stubDir, 'json').text = CHART_POD_TEMPLATE
        xnat.stageImage('registry.example/xnat:1.10.1')
        try {
            xnat.reset()
            fail('a reset whose switch of image would change no container should be refused')
        } catch (IllegalStateException e) {
            assertTrue(e.message.contains('xnat.k8s.containers'))
        }
        assertEquals(['--context test-context --namespace test-namespace get statefulset/xnat -o json'], calls())
    }

    /** The XNAT Helm chart's pod template, cut to what the tests read. */
    private static final String CHART_POD_TEMPLATE = '''{"spec": {"template": {"spec": {
            "initContainers": [{"name": "wait-for-postgres", "image": "busybox:1.36"},
                               {"name": "home-init", "image": "registry.example/xnat:1.10.2"}],
            "containers": [{"name": "xnat", "image": "registry.example/xnat:1.10.2",
                            "volumeMounts": [{"name": "xnat-home", "mountPath": "/data/xnat/home"},
                                             {"name": "home-plugins", "mountPath": "/data/xnat/home/plugins"},
                                             {"name": "xnatdata", "mountPath": "/data/xnat"}]}],
            "volumes": [{"name": "xnat-home", "emptyDir": {}},
                        {"name": "home-plugins", "emptyDir": {}},
                        {"name": "xnatdata", "persistentVolumeClaim": {"claimName": "xnat-data"}}]}}}}'''

    private static final String PERSISTENT_PLUGINS_POD_TEMPLATE = CHART_POD_TEMPLATE.replace(
            '{"name": "home-plugins", "emptyDir": {}}', '{"name": "home-plugins", "persistentVolumeClaim": {"claimName": "xnat-plugins"}}')

    @Test
    void aStagedImageGoesToEveryXnatContainerBeforeTheWorkloadStarts() {
        final KubernetesXnat xnat = new KubernetesXnat(kubectl())
        xnat.imageRepository = 'registry.example/xnat'
        new File(stubDir, 'json').text = CHART_POD_TEMPLATE
        new File(stubDir, 'stdout').text = 'pod/xnat-0'
        xnat.stageImage('1.10.1')
        assertTrue(xnat.hasStagedImage())
        xnat.start()
        assertFalse(xnat.hasStagedImage())
        final List<String> calls = calls()
        final int setImages = calls.findIndexOf { it.contains('set image statefulset/xnat home-init=registry.example/xnat:1.10.1 xnat=registry.example/xnat:1.10.1') }
        final int scaleUp = calls.findIndexOf { it.contains('scale statefulset/xnat --replicas=1') }
        assertTrue("every XNAT container switched (${setImages}) before scale-up (${scaleUp}): ${calls}", setImages >= 0 && setImages < scaleUp)
        assertFalse('an init container not named keeps its image', calls.any { it.contains('wait-for-postgres=') })
    }

    @Test
    void containersAreSwitchedByNameWhateverRepositoryTheyRunFrom() {
        final KubernetesXnat xnat = new KubernetesXnat(kubectl())
        xnat.imageRepository = 'registry.example/xnat'
        new File(stubDir, 'json').text = CHART_POD_TEMPLATE.replace('registry.example/xnat:1.10.2', 'registry.example/xnat-web:1.9.3.7')
        new File(stubDir, 'stdout').text = 'pod/xnat-0'
        xnat.stageImage('1.10.1')
        xnat.start()
        assertTrue(calls().any { it.contains('set image statefulset/xnat home-init=registry.example/xnat:1.10.1 xnat=registry.example/xnat:1.10.1') })
    }

    @Test
    void aFullImageReferenceIsUsedAsGiven() {
        final KubernetesXnat xnat = new KubernetesXnat(kubectl())
        new File(stubDir, 'json').text = CHART_POD_TEMPLATE
        new File(stubDir, 'stdout').text = 'pod/xnat-0'
        xnat.stageImage('registry.example/xnat-web:1.9.3.7')
        xnat.start()
        assertTrue(calls().any { it.contains('set image statefulset/xnat home-init=registry.example/xnat-web:1.9.3.7 xnat=registry.example/xnat-web:1.9.3.7') })
    }

    @Test
    void aBareTagNeedsTheImageRepository() {
        try {
            new KubernetesXnat(kubectl()).stageImage('1.10.1')
            fail('a tag with no repository to take it from should be refused')
        } catch (IllegalStateException e) {
            assertTrue(e.message.contains('xnat.k8s.image'))
        }
    }

    @Test
    void theTagOfAnImageReference() {
        assertEquals('1.10.1', KubernetesXnat.tagOf('ghcr.io/nrgxnat/xnat:1.10.1'))
        assertEquals('1.10.1', KubernetesXnat.tagOf('registry.example:5000/xnat:1.10.1'))
        assertEquals('latest', KubernetesXnat.tagOf('registry.example:5000/xnat'))
        assertEquals(null, KubernetesXnat.tagOf('ghcr.io/nrgxnat/xnat@sha256:0123456789abcdef'))
    }

    @Test
    void aWorkloadWithNoContainerOfTheConfiguredNamesIsRefused() {
        final KubernetesXnat xnat = new KubernetesXnat(kubectl())
        new File(stubDir, 'json').text = CHART_POD_TEMPLATE
        xnat.stageImage('registry.example/xnat:1.10.1')
        [['web'], ['home-init']].each { names ->
            xnat.containerNames = names
            try {
                xnat.start()
                fail("a switch of containers ${names}, none of them the main container, should be refused")
            } catch (IllegalStateException e) {
                assertTrue(e.message.contains('xnat.k8s.containers'))
            }
        }
        assertFalse(calls().any { it.contains('set image') || it.contains('scale') })
    }

    @Test
    void thePodOfAStatefulSetFollowsFromItsName() {
        assertEquals('xnat-0', KubernetesXnat.podOf('statefulset/xnat'))
        assertEquals('perf-xnat-0', KubernetesXnat.podOf('sts/perf-xnat'))
        try {
            KubernetesXnat.podOf('deployment/xnat')
            fail('the pod of a Deployment has no fixed name')
        } catch (IllegalArgumentException ignored) {}
    }

    @Test
    void pluginsAreNotInstalledWhereARestartLosesThem() {
        final KubernetesXnat xnat = new KubernetesXnat(kubectl())
        new File(stubDir, 'json').text = CHART_POD_TEMPLATE
        try {
            xnat.installPlugin('foo-1.0.jar', pluginJar('foo'))
            fail('a plugin should not be installed into an emptyDir')
        } catch (IllegalStateException e) {
            assertTrue(e.message.contains('the emptyDir volume home-plugins'))
        }
        assertFalse(new File(stubDir, 'stdin').exists())
    }

    @Test
    void pluginsAreNotRemovedFromWhereARestartLosesThem() {
        final KubernetesXnat xnat = new KubernetesXnat(kubectl())
        new File(stubDir, 'json').text = CHART_POD_TEMPLATE
        xnat.uninstallAllPlugins()
        xnat.uninstallPlugin('foo-1.0.jar')
        assertEquals(['--context test-context --namespace test-namespace get statefulset/xnat -o json'], calls())
    }

    @Test
    void anInstalledPluginMustBeLoadedAfterTheRestart() {
        final KubernetesXnat xnat = new KubernetesXnat(kubectl())
        new File(stubDir, 'json').text = PERSISTENT_PLUGINS_POD_TEMPLATE
        final File jar = pluginJar('foo')
        xnat.installPlugin('foo-1.0.jar', jar)
        assertEquals(jar.bytes, new File(stubDir, 'stdin').bytes)
        xnat.loadedPluginsReader = { -> ['bar'] }
        try {
            xnat.verifyInstalledPlugins()
            fail('a plugin XNAT did not load should fail the check')
        } catch (IllegalStateException e) {
            assertTrue(e.message.contains('foo from foo-1.0.jar'))
        }
        xnat.loadedPluginsReader = { -> ['bar', 'foo'] }
        xnat.verifyInstalledPlugins()
        xnat.uninstallAllPlugins()
        assertTrue(calls().last().contains("rm -f '/data/xnat/home/plugins'/*.jar"))
        xnat.loadedPluginsReader = { -> throw new AssertionError('nothing is left to check') }
        xnat.verifyInstalledPlugins()
    }

    @Test
    void theIdsOfThePluginsInAJar() {
        assertEquals(['foo'] as Set, KubernetesXnat.pluginIdsIn(pluginJar('foo')))
        assertEquals([] as Set, KubernetesXnat.pluginIdsIn(pluginJar(null)))
    }

    /** A jar holding one XNAT plugin with the id, or none when the id is null. */
    private File pluginJar(String id) {
        final File jar = File.createTempFile('plugin', '.jar', stubDir)
        jar.withOutputStream { stream ->
            final ZipOutputStream zip = new ZipOutputStream(stream)
            zip.putNextEntry(new ZipEntry(id ? "META-INF/xnat/${id}-plugin.properties" : 'META-INF/MANIFEST.MF'))
            zip.write((id ? "id=${id}\nname=Test plugin\n" : 'Manifest-Version: 1.0\n').getBytes('UTF-8'))
            zip.closeEntry()
            zip.finish()
        }
        jar
    }

    @Test
    void waitsForAPortThatOpensLate() {
        final int port = freePort()
        ServerSocket listener = null
        final Thread opener = Thread.start {
            sleep(1500)
            listener = new ServerSocket(port, 50, InetAddress.getByName('127.0.0.1'))
        }
        try {
            assertTrue(KubernetesXnat.waitForPort('127.0.0.1', port, 10000))
        } finally {
            opener.join()
            listener?.close()
        }
    }

    @Test
    void givesUpOnAPortThatNeverOpens() {
        final long started = System.currentTimeMillis()
        assertFalse(KubernetesXnat.waitForPort('127.0.0.1', freePort(), 1500))
        assertTrue(System.currentTimeMillis() - started >= 1500)
    }

    private static int freePort() {
        final ServerSocket probe = new ServerSocket(0, 50, InetAddress.getByName('127.0.0.1'))
        try {
            return probe.localPort
        } finally {
            probe.close()
        }
    }

}
