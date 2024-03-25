package org.nrg.testing.xnat.performance.reset

import org.nrg.testing.xnat.rest.XnatRestDriver

abstract class FixedScriptXnatReset implements PerformanceServerResetScript, SshExecutor {

    protected static final int COMMAND_TIMEOUT_MILLIS = 300000

    boolean actOverSsh

    FixedScriptXnatReset(boolean useSsh) {
        actOverSsh = useSsh
    }

    @Override
    void resetXnatServer() {
        if (actOverSsh) {
            executeCommandsOverSsh(commands())
        } else {
            commands().each { command ->
                final StringBuilder stdOut = new StringBuilder()
                final StringBuilder stdErr = new StringBuilder()
                final Process proc = command.execute()
                proc.consumeProcessOutput(stdOut, stdErr)
                proc.waitForOrKill(COMMAND_TIMEOUT_MILLIS)
            }
        }
        XnatRestDriver.invalidateCachedCredentials()
    }

    abstract List<String> commands()

}
