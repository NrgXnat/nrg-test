package org.nrg.testing.tests

import org.nrg.testing.xnat.kubernetes.Kubectl
import org.nrg.testing.xnat.kubernetes.KubectlException
import org.nrg.testing.xnat.kubernetes.KubernetesXnat
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

import java.nio.file.Files

import static org.testng.AssertJUnit.assertEquals
import static org.testng.AssertJUnit.assertFalse
import static org.testng.AssertJUnit.assertTrue
import static org.testng.AssertJUnit.fail

/** Runs {@link Kubectl} and {@link KubernetesXnat} against a stub kubectl that records what it was asked to do. */
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
            printf '%s\\n' "$*" >> "$d/calls"
            case " $* " in *" -i "*) cat > "$d/stdin";; esac
            [ -f "$d/sleep" ] && sleep "$(cat "$d/sleep")"
            [ -f "$d/stdout" ] && cat "$d/stdout"
            [ -f "$d/stderr" ] && cat "$d/stderr" >&2
            exit "$(cat "$d/status" 2>/dev/null || echo 0)"
            '''.stripIndent()
        assertTrue(stub.setExecutable(true))
    }

    private Kubectl kubectl() {
        new Kubectl(stub.path, 'test-context', 'test-namespace')
    }

    private List<String> recordedArguments() {
        new File(stubDir, 'args').readLines()
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
    void aStagedImageIsSetBeforeTheWorkloadStarts() {
        final KubernetesXnat xnat = new KubernetesXnat(kubectl())
        xnat.container = 'xnat'
        xnat.imageRepository = 'registry.example/xnat'
        xnat.stageImage('1.10.1')
        assertTrue(xnat.hasStagedImage())
        new File(stubDir, 'stdout').text = 'pod/xnat-0'
        xnat.start()
        assertFalse(xnat.hasStagedImage())
        final List<String> calls = new File(stubDir, 'calls').readLines()
        final int setImage = calls.findIndexOf { it.contains('set image statefulset/xnat xnat=registry.example/xnat:1.10.1') }
        final int scaleUp = calls.findIndexOf { it.contains('scale statefulset/xnat --replicas=1') }
        assertTrue("image set (${setImage}) before scale-up (${scaleUp}): ${calls}", setImage >= 0 && setImage < scaleUp)
    }

}
