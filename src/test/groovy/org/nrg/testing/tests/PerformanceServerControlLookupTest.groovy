package org.nrg.testing.tests

import org.nrg.testing.xnat.performance.control.KubernetesServerControl
import org.nrg.testing.xnat.performance.control.PerformanceServerControlLookup
import org.nrg.testing.xnat.performance.control.SshServerControl
import org.testng.annotations.Test

import static org.testng.AssertJUnit.assertTrue
import static org.testng.AssertJUnit.fail

class PerformanceServerControlLookupTest {

    @Test
    void eachPlatformHasItsControl() {
        assertTrue(PerformanceServerControlLookup.lookup(null) instanceof SshServerControl)
        assertTrue(PerformanceServerControlLookup.lookup('ssh') instanceof SshServerControl)
        assertTrue(PerformanceServerControlLookup.lookup('kubernetes') instanceof KubernetesServerControl)
    }

    @Test
    void anUnknownControlIsRefusedByName() {
        try {
            PerformanceServerControlLookup.lookup('k8s')
            fail('an unknown server control should be refused')
        } catch (IllegalArgumentException e) {
            assertTrue(e.message.contains('k8s'))
            assertTrue(e.message.contains('[ssh, kubernetes]'))
        }
    }

}
