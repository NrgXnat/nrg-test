package org.nrg.testing.xnat.performance.reset

abstract class FixedScriptXnatReset implements PerformanceServerResetScript {

    protected static final int COMMAND_TIMEOUT_MILLIS = 300000

    @Override
    void resetXnatServer() {
        commands().each { command ->
            final StringBuilder stdOut = new StringBuilder()
            final StringBuilder stdErr = new StringBuilder()
            final Process proc = command.execute()
            proc.consumeProcessOutput(stdOut, stdErr)
            proc.waitForOrKill(COMMAND_TIMEOUT_MILLIS)
        }
    }

    abstract List<String> commands()

}
