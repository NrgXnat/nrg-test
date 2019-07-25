package org.nrg.testing.xnat.ssh

import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import net.schmizz.sshj.xfer.FileSystemFile
import org.apache.commons.lang3.time.StopWatch
import org.apache.log4j.Logger
import org.nrg.testing.TimeUtils
import org.nrg.testing.util.ResourceLoader
import org.nrg.testing.xnat.conf.Settings
import org.nrg.testing.xnat.conf.XNATProperties
import org.testng.AssertJUnit

class SSHConnection {

    private static final Logger LOGGER = Logger.getLogger(SSHConnection)
    private SSHClient sshClient

    private void initiateConnection() {
        sshClient = new SSHClient()
        sshClient.addHostKeyVerifier(new PromiscuousVerifier())
        sshClient.connect(Settings.HOSTURL)
        sshClient.authPublickey(Settings.SSH_USER, sshClient.loadKeys(Settings.SSH_KEY.path))
    }

    private void disconnect() {
        sshClient.disconnect()
    }

    boolean testSSH() {
        if (Settings.SSH_USER == null) {
            LOGGER.info("No username is available for SSH, so all tests requiring SSH access will be skipped. Set ${XNATProperties.SSH_USER} if SSH is needed.")
        } else if (!Settings.SSH_KEY.exists()) {
            LOGGER.info("No SSH key is available, so all tests requiring SSH access will be skipped. Set ${XNATProperties.SSH_PRIVATE_KEY_NAME} if SSH is needed.")
        } else {
            try {
                if (executeSingleCommand('echo \'Hello world\'').exitStatus == 0) {
                    LOGGER.info('SSH appears to be working...')
                    return true
                } else {
                    LOGGER.warn('Simple echo to test SSH failed. All tests requiring SSH access will be skipped.')
                }
            } catch (Exception e) {
                LOGGER.warn('All tests requiring SSH access will be skipped because SSH doesn\'t seem to be working: ', e)
            }
        }
        false
    }

    void pushScripts() {
        initiateConnection()
        executeCommandWithCurrentConnection('mkdir -p ~/bin')
        final File script = ResourceLoader.copyAndGetResource("manage_${Settings.TOMCAT_VERSION}.sh")
        sshClient.newSCPFileTransfer().upload(new FileSystemFile(script), 'bin/')
        executeCommandWithCurrentConnection("chmod +x bin/${script.name}")
        disconnect()
    }

    SSHCommandResult executeCommandWithCurrentConnection(String command) {
        new SSHCommandResult(sshClient.startSession().exec(command))
    }

    SSHCommandResult executeSingleCommand(String command) {
        initiateConnection()
        final SSHCommandResult results = executeCommandWithCurrentConnection(command)
        disconnect()
        results
    }

    void restartTomcat() {
        manageTomcat('restart')
        waitForTomcat()
    }

    void stopTomcat() {
        manageTomcat('stop')
    }

    void startTomcat() {
        manageTomcat('start')
        waitForTomcat()
    }

    private void manageTomcat(String command) {
        LOGGER.info("Sending command to tomcat: ${command}...")
        final SSHCommandResult results = executeSingleCommand("bin/manage_${Settings.TOMCAT_VERSION}.sh ${command}")
        LOGGER.info(results.stdOut)
        AssertJUnit.assertEquals(0, results.exitStatus)
    }

    private void waitForTomcat() {
        final StopWatch stopWatch = TimeUtils.launchStopWatch()
        while (true) {
            TimeUtils.checkStopWatch(stopWatch, 500, 'Tomcat didn\'t come back after restarting/starting it with SSH')
            LOGGER.info('Waiting for tomcat to start back up...')
            if (Settings.mainCredentials().get(Settings.BASEURL).statusCode == 200) break
            TimeUtils.sleep(10000)
        }
        TimeUtils.sleep(15000) // give it 15 extra seconds to just wait for tomcat to more fully be ready
    }

}
