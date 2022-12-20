package org.nrg.testing.xnat.performance.reset

import org.nrg.testing.TestNgUtils
import org.nrg.testing.xnat.conf.Settings
import org.nrg.testing.xnat.rest.XnatRestDriver
import org.nrg.testing.xnat.ssh.SSHCommandResult
import org.nrg.testing.xnat.ssh.SSHConnection

import static org.testng.AssertJUnit.assertEquals

abstract class FixedScriptXnatReset implements PerformanceServerResetScript {

    protected static final int COMMAND_TIMEOUT_MILLIS = 300000

    boolean actOverSsh

    FixedScriptXnatReset(boolean useSsh) {
        actOverSsh = useSsh
    }

    @Override
    void resetXnatServer() {
        if (actOverSsh) {
            TestNgUtils.assumeTrue(Settings.SSH_FUNCTIONS, 'SSH setup is required for this reset method')
            final SSHConnection sshConnection = new SSHConnection()
            sshConnection.initiateConnection()
            commands().each { command ->
                final SSHCommandResult result = sshConnection.executeCommandWithCurrentConnection(command)
                assertEquals(0, result.exitStatus)
            }
            sshConnection.disconnect()
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
