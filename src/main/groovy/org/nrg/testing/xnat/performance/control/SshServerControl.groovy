package org.nrg.testing.xnat.performance.control

import org.nrg.testing.xnat.conf.Settings
import org.nrg.testing.xnat.ssh.SSHConnection
import org.nrg.xnat.pogo.PluginRegistry
import org.nrg.xnat.pogo.XnatDeployment
import org.nrg.xnat.pogo.plugins.BitbucketDownloadsDerivation

/** Tomcat on a host reached over SSH, with the XNAT version installed as a WAR. */
class SshServerControl implements PerformanceServerControl {

    @Override
    void stopXnat() {
        new SSHConnection().stopTomcat()
    }

    @Override
    void deployXnat(XnatDeployment deployment) {
        final BitbucketDownloadsDerivation xnatDownloads = new BitbucketDownloadsDerivation(PluginRegistry.XNAT_DEV, 'xnat-web')
        final String downloadUrl = xnatDownloads.apply(deployment.xnatVersionString == '1.7.6' ? '1.7.6-tc8' : deployment.xnatVersionString)
        Settings.PERFORMANCE_PLUGIN_INSTALLER.installWarWithUrl(downloadUrl)
    }

    @Override
    void waitForXnat() {
        SSHConnection.waitForTomcat()
    }

}
