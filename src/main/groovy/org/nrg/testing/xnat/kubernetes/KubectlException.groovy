package org.nrg.testing.xnat.kubernetes

class KubectlException extends RuntimeException {

    final List<String> arguments
    final int exitStatus

    KubectlException(List<String> arguments, int exitStatus, String detail) {
        super("kubectl ${arguments.join(' ')} failed${exitStatus >= 0 ? " (exit ${exitStatus})" : ''}: ${detail}")
        this.arguments = arguments
        this.exitStatus = exitStatus
    }

}
