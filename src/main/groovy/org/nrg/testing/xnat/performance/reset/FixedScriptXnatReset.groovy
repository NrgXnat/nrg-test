package org.nrg.testing.xnat.performance.reset

abstract class FixedScriptXnatReset implements PerformanceServerResetScript {

    protected static final int COMMAND_TIMEOUT = 300000

    @Override
    void resetXnatServer() {
        commandsList().each { command ->
            final StringBuilder stdOut = new StringBuilder()
            final StringBuilder stdErr = new StringBuilder()
            final Process proc = command.execute()
            proc.consumeProcessOutput(stdOut, stdErr)
            proc.waitForOrKill(COMMAND_TIMEOUT)
        }
    }

    abstract List<String> commandsList()

}
