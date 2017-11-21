package org.nrg.testing.xnat.ssh;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.xfer.FileSystemFile;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.Logger;
import org.nrg.testing.CommonUtils;
import org.nrg.testing.util.ResourceLoader;
import org.nrg.testing.xnat.conf.Settings;
import org.nrg.testing.xnat.conf.XNATProperties;
import org.testng.Assert;
import org.testng.AssertJUnit;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SSHConnection {

    private static final Logger LOGGER = Logger.getLogger(SSHConnection.class);

    private static SSHClient getConnection() {
        try {
            SSHClient ssh = new SSHClient();
            ssh.addHostKeyVerifier(new PromiscuousVerifier());
            ssh.connect(Settings.HOSTURL);
            KeyProvider keys = ssh.loadKeys(Settings.SSH_KEY.getPath());
            ssh.authPublickey(Settings.SSH_USER, keys);
            return ssh;
        } catch (Exception e) {
            LOGGER.fatal("Failed to get SSH connection.", e);
            throw new RuntimeException(e);
        }
    }

    public static boolean testSSH() {
        if (!Settings.SSH_KEY.exists()) {
            LOGGER.info(String.format("No SSH key is available, so all tests requiring SSH access will be skipped. Set %s if SSH is needed.", XNATProperties.SSH_PRIVATE_KEY_NAME));
            return false;
        }
        if (Settings.SSH_USER == null) {
            LOGGER.info(String.format("No username is available for SSH, so all tests requiring SSH access will be skipped. Set %s if SSH is needed.", XNATProperties.SSH_USER));
            return false;
        }
        try {
            AssertJUnit.assertEquals(0, executeCommand("echo 'Hello world'").getExitStatus());
            LOGGER.info("SSH appears to be working...");
            return true;
        } catch (Exception | Error e) {
            LOGGER.warn("All tests requiring SSH access will be skipped because SSH doesn't seem to be working: ", e);
            return false;
        }
    }

    public static void pushScripts() {
        SSHClient ssh = getConnection();
        try {
            executeCommand("mkdir -p ~/bin");
            pushScript(String.format("manage_%s.sh", Settings.TOMCAT_VERSION), ssh);
            ssh.disconnect();
        } catch (Throwable e) {
            LOGGER.fatal("Failed to transfer scripts over SCP.", e);
            throw new RuntimeException(e);
        }

    }

    private static void pushScript(String scriptName, SSHClient ssh) {
        try {
            final File script = ResourceLoader.copyAndGetResource(scriptName);
            ssh.newSCPFileTransfer().upload(new FileSystemFile(script), "bin/");
            executeCommand(String.format("chmod +x bin/%s", script.getName()));
        } catch (Throwable e) {
            LOGGER.fatal("Failed to transfer scripts over SCP.", e);
            throw new RuntimeException(e);
        }
    }

    public static SSHCommandResult executeCommand(String command) {
        try {
            SSHClient ssh = getConnection();
            Session session = ssh.startSession();
            Session.Command sessionCommand = session.exec(command);
            final SSHCommandResult results = SSHCommandResult.read(sessionCommand);
            ssh.disconnect();
            return results;
        } catch (Exception e) {
            LOGGER.warn("Failed to connect to test server with SSH", e);
            Assert.fail("Failed to connect to test server with SSH:"); throw new Error("Unreachable");
        }
    }

    public static void restartTomcat() {
        manageTomcat("restart");
        waitForTomcat();
    }

    public static void stopTomcat() {
        manageTomcat("stop");
    }

    public static void startTomcat() {
        manageTomcat("start");
        waitForTomcat();
    }

    private static void manageTomcat(String command) {
        LOGGER.info(String.format("Sending command to tomcat: %s...", command));
        final SSHCommandResult results = executeCommand(String.format("bin/manage_%s.sh %s", Settings.TOMCAT_VERSION, command));
        LOGGER.info(results.getStdOut());
        AssertJUnit.assertEquals(0, results.getExitStatus());
    }

    private static void waitForTomcat() {
        StopWatch stopWatch = CommonUtils.launchStopWatch();
        while (true) {
            CommonUtils.checkStopWatch(stopWatch, 500, "Tomcat didn't come back after restarting/starting it with SSH");
            LOGGER.info("Waiting for tomcat to start back up...");
            if (Settings.mainCredentials().get(Settings.BASEURL).getStatusCode() == 200) break;
            CommonUtils.sleep(10000);
        }
        CommonUtils.sleep(15000); // give it 15 extra seconds to just wait for tomcat to more fully be ready
    }
}
