package org.nrg.testing.xnat.performance.reset

import org.apache.log4j.Logger
import org.nrg.testing.TestNgUtils
import org.nrg.testing.xnat.conf.Settings
import org.nrg.testing.xnat.ssh.SSHCommandResult
import org.nrg.testing.xnat.ssh.SSHConnection

import static org.testng.AssertJUnit.assertEquals

trait SshExecutor {

    void executeCommandsOverSsh(List<String> commands) {
        TestNgUtils.assumeTrue(Settings.SSH_FUNCTIONS, 'SSH setup is required for this reset method')
        final SSHConnection sshConnection = new SSHConnection()
        sshConnection.initiateConnection()
        commands.each { command ->
            Logger.getLogger(SshExecutor).info("Executing: ${command}")
            final SSHCommandResult result = sshConnection.executeCommandWithCurrentConnection(command)
            assertEquals(0, result.exitStatus)
        }
        sshConnection.disconnect()
    }

}