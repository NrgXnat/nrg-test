package org.nrg.testing.tests

import org.nrg.testing.xnat.performance.ImageTaggedXnatDeployment
import org.nrg.xnat.pogo.XnatDeployment
import org.nrg.xnat.versions.Xnat_1_10_1
import org.testng.annotations.Test

import static org.testng.AssertJUnit.assertEquals
import static org.testng.AssertJUnit.assertFalse
import static org.testng.AssertJUnit.assertTrue
import static org.testng.AssertJUnit.fail

class ImageTaggedXnatDeploymentTest {

    @Test
    void aPlainVersionIsItsOwnImageTag() {
        final XnatDeployment deployment = ImageTaggedXnatDeployment.parse('1.10.1')
        assertFalse(deployment instanceof ImageTaggedXnatDeployment)
        assertEquals('1.10.1', deployment.id)
        assertEquals('1.10.1', ImageTaggedXnatDeployment.imageTagFor(deployment))
    }

    @Test
    void aTaggedDeploymentRunsItsTagAsTheVersionItNames() {
        final XnatDeployment deployment = ImageTaggedXnatDeployment.parse('1.10.2-develop-2026.09.24-SNAPSHOT=1.10.1')
        assertTrue(deployment instanceof ImageTaggedXnatDeployment)
        assertEquals('1.10.2-develop-2026.09.24-SNAPSHOT', deployment.id)
        assertEquals('1.10.2-develop-2026.09.24-SNAPSHOT', ImageTaggedXnatDeployment.imageTagFor(deployment))
        assertEquals('1.10.1', deployment.xnatVersionString)
        assertEquals(Xnat_1_10_1, deployment.xnatVersion)
    }

    @Test
    void aTagCannotBeEmpty() {
        try {
            ImageTaggedXnatDeployment.parse('=1.10.1')
            fail('an empty tag should be refused')
        } catch (IllegalArgumentException ignored) {}
    }

}
