package org.nrg.testing.xnat.kubernetes

import groovy.util.logging.Log4j

import java.util.concurrent.TimeUnit

/**
 * Runs kubectl against one namespace. With no context it uses kubectl's own default, which inside a pod is the
 * pod's service account, so the same code drives a cluster from a workstation or from a Job in the cluster.
 */
@Log4j
class Kubectl {

    public static final long DEFAULT_TIMEOUT_SECONDS = 300

    final String executable
    final String context
    final String namespace

    Kubectl(String executable, String context, String namespace) {
        if (!namespace) {
            throw new IllegalArgumentException('A namespace is required')
        }
        this.executable = executable ?: 'kubectl'
        this.context = context ?: null
        this.namespace = namespace
    }

    /** The cluster and namespace this instance acts on, as {@code <context>/<namespace>}; the context is {@code in-cluster} when unset. */
    String getTarget() {
        "${context ?: 'in-cluster'}/${namespace}"
    }

    List<String> command(List<String> arguments) {
        final List<String> command = [executable]
        if (context) {
            command.addAll(['--context', context])
        }
        command.addAll(['--namespace', namespace])
        command.addAll(arguments)
        command
    }

    /** Runs kubectl with the given arguments and returns its standard output, failing on a non-zero exit or a timeout. */
    String run(List<String> arguments, long timeoutSeconds = DEFAULT_TIMEOUT_SECONDS, File input = null) {
        final List<String> command = command(arguments)
        log.info("Running: ${command.join(' ')}")
        final ProcessBuilder builder = new ProcessBuilder(command)
        if (input != null) {
            builder.redirectInput(input)
        }
        final Process process = builder.start()
        if (input == null) {
            process.outputStream.close()
        }
        final ByteArrayOutputStream stdOut = new ByteArrayOutputStream()
        final ByteArrayOutputStream stdErr = new ByteArrayOutputStream()
        final Thread outReader = process.consumeProcessOutputStream(stdOut)
        final Thread errReader = process.consumeProcessErrorStream(stdErr)
        if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
            process.destroyForcibly()
            throw new KubectlException(arguments, -1, "timed out after ${timeoutSeconds} s")
        }
        outReader.join(10000)
        errReader.join(10000)
        if (process.exitValue() != 0) {
            throw new KubectlException(arguments, process.exitValue(), stdErr.toString('UTF-8').trim())
        }
        stdOut.toString('UTF-8')
    }

    /**
     * Runs kubectl with the given arguments and writes its standard output to a file, for output that isn't text or
     * that is too large to hold, failing on a non-zero exit or a timeout.
     */
    void runToFile(List<String> arguments, File output, long timeoutSeconds = DEFAULT_TIMEOUT_SECONDS) {
        final List<String> command = command(arguments)
        log.info("Running: ${command.join(' ')} > ${output}")
        final Process process = new ProcessBuilder(command).redirectOutput(output).start()
        process.outputStream.close()
        final ByteArrayOutputStream stdErr = new ByteArrayOutputStream()
        final Thread errReader = process.consumeProcessErrorStream(stdErr)
        if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
            process.destroyForcibly()
            throw new KubectlException(arguments, -1, "timed out after ${timeoutSeconds} s")
        }
        errReader.join(10000)
        if (process.exitValue() != 0) {
            throw new KubectlException(arguments, process.exitValue(), stdErr.toString('UTF-8').trim())
        }
    }

    /** Runs a shell script in a container of a pod. A null container means the pod's default container. */
    String exec(String pod, String container, String script, long timeoutSeconds = DEFAULT_TIMEOUT_SECONDS) {
        run(execArguments(pod, container, false) + ['--', 'sh', '-c', script], timeoutSeconds)
    }

    /** As {@link #exec}, writing the script's standard output to a file. */
    void execToFile(String pod, String container, String script, File output, long timeoutSeconds = DEFAULT_TIMEOUT_SECONDS) {
        runToFile(execArguments(pod, container, false) + ['--', 'sh', '-c', script], output, timeoutSeconds)
    }

    /** Writes a container's log to a file. A null container means the pod's default container. */
    void logsToFile(String pod, String container, File output, long timeoutSeconds = DEFAULT_TIMEOUT_SECONDS) {
        runToFile(['logs', pod] + (container ? ['-c', container] : []), output, timeoutSeconds)
    }

    /** Writes a local file to a path in a container, through a temporary name so a reader never sees it partly written. */
    void upload(File source, String pod, String container, String remotePath, long timeoutSeconds = DEFAULT_TIMEOUT_SECONDS) {
        requireSafePath(remotePath)
        final String staged = "${remotePath}.uploading"
        run(execArguments(pod, container, true) + ['--', 'sh', '-c', "cat > '${staged}' && mv '${staged}' '${remotePath}'".toString()],
                timeoutSeconds, source)
    }

    void scale(String workload, int replicas) {
        run(['scale', workload, "--replicas=${replicas}".toString()])
    }

    /** Sets one image on several containers or init containers of a workload's pod template, in one change. */
    void setImages(String workload, List<String> containers, String image) {
        run(['set', 'image', workload] + containers.collect { name -> "${name}=${image}".toString() })
    }

    boolean podExists(String pod) {
        run(['get', 'pods', '--field-selector', "metadata.name=${pod}".toString(), '-o', 'name']).trim() != ''
    }

    void waitForPodDeletion(String pod, long timeoutSeconds) {
        if (podExists(pod)) {
            run(['wait', '--for=delete', "pod/${pod}".toString(), "--timeout=${timeoutSeconds}s".toString()], timeoutSeconds + 30)
        }
    }

    /** Waits for a pod to exist and then to report Ready, within one overall timeout. */
    void waitForPodReady(String pod, long timeoutSeconds) {
        final long deadline = System.currentTimeMillis() + timeoutSeconds * 1000
        while (!podExists(pod)) {
            if (System.currentTimeMillis() > deadline) {
                throw new KubectlException(['get', 'pod', pod], -1, "pod did not appear within ${timeoutSeconds} s")
            }
            sleep(2000)
        }
        final long remaining = Math.max(1, (long) ((deadline - System.currentTimeMillis()) / 1000))
        run(['wait', '--for=condition=Ready', "pod/${pod}".toString(), "--timeout=${remaining}s".toString()], remaining + 30)
    }

    private static List<String> execArguments(String pod, String container, boolean stdin) {
        final List<String> arguments = ['exec']
        if (stdin) {
            arguments << '-i'
        }
        arguments << pod
        if (container) {
            arguments.addAll(['-c', container])
        }
        arguments
    }

    /** Rejects paths that could escape the single quotes they are used in, or that are not absolute. */
    static void requireSafePath(String path) {
        if (!path?.startsWith('/') || path.contains("'") || path.contains('..')) {
            throw new IllegalArgumentException("Refusing unsafe container path: ${path}")
        }
    }

}
