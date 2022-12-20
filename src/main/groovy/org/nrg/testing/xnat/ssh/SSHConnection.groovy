package org.nrg.testing.xnat.ssh

import groovy.util.logging.Log4j
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.transport.verification.HostKeyVerifier
import net.schmizz.sshj.transport.verification.OpenSSHKnownHosts
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import org.apache.commons.lang3.time.StopWatch
import org.nrg.testing.TimeUtils
import org.nrg.testing.xnat.conf.Settings
import org.nrg.testing.xnat.conf.XNATProperties
import org.nrg.testing.xnat.rest.XnatRestDriver
import org.testng.AssertJUnit

import java.nio.file.Paths

@Log4j
class SSHConnection {

    private SSHClient sshClient
    private static final HostKeyVerifier KNOWN_HOSTS = new OpenSSHKnownHosts(Paths.get(System.getProperty('user.home'), '.ssh', 'known_hosts').toFile())

    void initiateConnection() {
        sshClient = new SSHClient()
        sshClient.addHostKeyVerifier(KNOWN_HOSTS)
        sshClient.connect(Settings.HOSTURL)
        sshClient.authPublickey(Settings.SSH_USER, sshClient.loadKeys(Settings.SSH_KEY.path))
    }

    void disconnect() {
        sshClient.disconnect()
    }

    boolean testSSH() {
        if (Settings.SSH_USER == null) {
            log.info("No username is available for SSH, so all tests requiring SSH access will be skipped. Set ${XNATProperties.SSH_USER} if SSH is needed.")
        } else if (!Settings.SSH_KEY.exists()) {
            log.info("No SSH key is available, so all tests requiring SSH access will be skipped. Set ${XNATProperties.SSH_PRIVATE_KEY_NAME} if SSH is needed.")
        } else {
            try {
                if (executeSingleCommand('echo \'Hello world\'').exitStatus == 0) {
                    log.info('SSH appears to be working...')
                    return true
                } else {
                    log.warn('Simple echo to test SSH failed. All tests requiring SSH access will be skipped.')
                }
            } catch (Exception e) {
                log.warn('All tests requiring SSH access will be skipped because SSH doesn\'t seem to be working: ', e)
            }
        }
        false
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
        manageTomcat('restart', Settings.TOMCAT_CONTROLLER.restartCommand)
        waitForTomcat()
    }

    void stopTomcat() {
        XnatRestDriver.invalidateCachedCredentials()
        manageTomcat('stop', Settings.TOMCAT_CONTROLLER.stopCommand)
    }

    void startTomcat() {
        XnatRestDriver.invalidateCachedCredentials()
        manageTomcat('start', Settings.TOMCAT_CONTROLLER.startCommand)
        waitForTomcat()
    }

    static void waitForTomcat() {
        final StopWatch stopWatch = TimeUtils.launchStopWatch()
        while (true) {
            TimeUtils.checkStopWatch(stopWatch, 500, 'Tomcat didn\'t come back after restarting/starting it with SSH')
            log.info('Waiting for tomcat to start back up...')
            try {
                if (Settings.mainCredentials().get(Settings.BASEURL).statusCode == 200) {
                    break
                }
            } catch (ConnectException ignored) {}
            TimeUtils.sleep(10000)
        }
        TimeUtils.sleep(15000) // give it 15 extra seconds to just wait for tomcat to more fully be ready
    }

    private void manageTomcat(String commandName, String commandString) {
        log.info("Sending command to tomcat: ${commandName}...")
        final SSHCommandResult results = executeSingleCommand(commandString)
        log.info(results.stdOut)
        log.error(results.stdErr)
        AssertJUnit.assertEquals(0, results.exitStatus)
    }

}
