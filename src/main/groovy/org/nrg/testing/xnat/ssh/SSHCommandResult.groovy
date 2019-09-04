package org.nrg.testing.xnat.ssh

import net.schmizz.sshj.connection.channel.direct.Session
import org.apache.commons.io.IOUtils

class SSHCommandResult {
    String stdOut
    String stdErr
    String errorMessage
    int exitStatus

    SSHCommandResult(Session.Command executedCommand) {
        setStdOut(IOUtils.toString(executedCommand.inputStream, 'UTF-8'))
        setStdErr(IOUtils.toString(executedCommand.errorStream, 'UTF-8'))
        executedCommand.close()
        setErrorMessage(executedCommand.exitErrorMessage)
        setExitStatus(executedCommand.exitStatus)
    }
    
}
