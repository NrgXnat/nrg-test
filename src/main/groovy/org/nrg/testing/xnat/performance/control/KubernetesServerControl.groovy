package org.nrg.testing.xnat.performance.control

import org.nrg.testing.xnat.conf.Settings
import org.nrg.testing.xnat.kubernetes.KubernetesXnat
import org.nrg.testing.xnat.rest.XnatRestDriver
import org.nrg.testing.xnat.ssh.SSHConnection
import org.nrg.xnat.pogo.XnatDeployment

/**
 * XNAT as a Kubernetes workload, with the XNAT version as an image tag. The pod stays up while plugins change, since
 * they are written into its volume through it; the Kubernetes reset restarts it on the staged image.
 */
class KubernetesServerControl implements PerformanceServerControl {

    @Override
    void stopXnat() {
        XnatRestDriver.invalidateCachedCredentials()
    }

    @Override
    void deployXnat(XnatDeployment deployment) {
        Settings.kubernetesXnat().stageImageForVersion(deployment.xnatVersionString)
    }

    @Override
    void waitForXnat() {
        final KubernetesXnat xnat = Settings.kubernetesXnat()
        xnat.applyStagedImage()
        xnat.waitForReady()
        SSHConnection.waitForTomcat()
    }

}
