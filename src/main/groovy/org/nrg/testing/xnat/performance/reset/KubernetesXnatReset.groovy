package org.nrg.testing.xnat.performance.reset

import org.nrg.testing.xnat.conf.Settings
import org.nrg.testing.xnat.rest.XnatRestDriver

/**
 * Resets an XNAT on Kubernetes the way {@link InternalXnatReset} resets one on a VM: data directories emptied, a fresh
 * database, and XNAT restarted, all through kubectl. Selected with {@code xnat.performance.resetId=kubernetes}.
 */
class KubernetesXnatReset implements PerformanceServerResetScript {

    @Override
    void resetXnatServer() {
        Settings.kubernetesXnat().reset()
        XnatRestDriver.invalidateCachedCredentials()
    }

}
