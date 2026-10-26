package org.nrg.testing.xnat.performance.control

import org.nrg.xnat.pogo.XnatDeployment

/**
 * How the performance tests take XNAT down, put a version in place and bring it back, for the platform it runs on.
 * Chosen with {@code xnat.performance.serverControl}: {@code ssh} (the default) or {@code kubernetes}.
 */
interface PerformanceServerControl {

    /** Takes XNAT out of service before its plugins and version change. */
    void stopXnat()

    /** Puts the deployment's XNAT version in place for the next start. */
    void deployXnat(XnatDeployment deployment)

    /** Returns once XNAT is up and answering REST requests. */
    void waitForXnat()

}
