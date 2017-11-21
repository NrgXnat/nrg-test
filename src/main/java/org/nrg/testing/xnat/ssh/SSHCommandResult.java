package org.nrg.testing.xnat.ssh;

import net.schmizz.sshj.connection.channel.direct.Session;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

public class SSHCommandResult {
    private String stdOut, stdErr, errorMessage;
    private int exitStatus;

    public static SSHCommandResult read(Session.Command executedCommand) throws IOException {
        final SSHCommandResult result = new SSHCommandResult();
        result.setStdOut(IOUtils.toString(executedCommand.getInputStream(), "UTF-8"));
        result.setStdErr(IOUtils.toString(executedCommand.getErrorStream(), "UTF-8"));
        result.setErrorMessage(executedCommand.getExitErrorMessage());
        result.setExitStatus(executedCommand.getExitStatus());
        return result;
    }

    public String getStdOut() {
        return stdOut;
    }

    public void setStdOut(String stdOut) {
        this.stdOut = stdOut;
    }

    public String getStdErr() {
        return stdErr;
    }

    public void setStdErr(String stdErr) {
        this.stdErr = stdErr;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getExitStatus() {
        return exitStatus;
    }

    public void setExitStatus(Integer exitStatus) {
        this.exitStatus = (exitStatus == null) ? 0 : exitStatus;
    }

}
