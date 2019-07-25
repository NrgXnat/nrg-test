package org.nrg.testing.tests

import org.nrg.testing.UnitTestFilter
import org.nrg.testing.annotations.DisallowXnatVersion
import org.nrg.testing.annotations.RequireXnatVersion
import org.nrg.testing.listeners.interceptors.filters.ProhibitedTestFilter
import org.nrg.testing.xnat.versions.*
import org.testng.IMethodInstance
import org.testng.annotations.Listeners
import org.testng.annotations.Test

import static org.testng.AssertJUnit.*

@Listeners(UnitTestFilter)
@RequireXnatVersion(allowedVersions = [Xnat_1_6dev, Xnat_1_7_2])
class RequireXnatVersionTest {

    private final ProhibitedTestFilter testFilter = new ProhibitedTestFilter()

    @Test
    @RequireXnatVersion(allowedVersions = Xnat_1_6dev)
    void requiredClassAndMethod() {
        final IMethodInstance thisTest = UnitTestFilter.getInstance('requiredClassAndMethod')

        assertTrue (testFilter.isTestAllowed(thisTest, Xnat_1_6dev))
        assertFalse(testFilter.isTestAllowed(thisTest, Xnat_1_7_2))
        assertFalse(testFilter.isTestAllowed(thisTest, Xnat_1_7dev))
    }

    @Test
    @DisallowXnatVersion(disallowedVersions = Xnat_1_7_2)
    void requiredClassDisallowedMethod() {
        final IMethodInstance thisTest = UnitTestFilter.getInstance('requiredClassDisallowedMethod')

        assertTrue (testFilter.isTestAllowed(thisTest, Xnat_1_6dev))
        assertFalse(testFilter.isTestAllowed(thisTest, Xnat_1_7_2))
        assertFalse(testFilter.isTestAllowed(thisTest, Xnat_1_7_3))
    }

}
