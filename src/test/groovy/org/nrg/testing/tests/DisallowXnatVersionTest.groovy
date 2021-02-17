package org.nrg.testing.tests

import org.nrg.testing.unit.UnitTestFilter
import org.nrg.testing.annotations.DisallowXnatVersion
import org.nrg.testing.annotations.RequireXnatVersion
import org.nrg.testing.listeners.interceptors.filters.ProhibitedTestFilter
import org.nrg.xnat.versions.*
import org.testng.IMethodInstance
import org.testng.annotations.Listeners
import org.testng.annotations.Test

import static org.testng.AssertJUnit.assertFalse
import static org.testng.AssertJUnit.assertTrue

@Listeners(UnitTestFilter)
@DisallowXnatVersion(disallowedVersions = [Xnat_1_6dev, Xnat_1_7_2])
class DisallowXnatVersionTest {

    private final ProhibitedTestFilter testFilter = new ProhibitedTestFilter()

    @Test
    @DisallowXnatVersion(disallowedVersions = [Xnat_1_6dev, Xnat_1_7_3])
    void disallowedClassAndMethod() {
        final IMethodInstance thisTest = UnitTestFilter.getInstance('disallowedClassAndMethod')

        assertTrue (testFilter.isTestAllowed(thisTest, Xnat_1_7_5))
        assertFalse(testFilter.isTestAllowed(thisTest, Xnat_1_6dev))
        assertFalse(testFilter.isTestAllowed(thisTest, Xnat_1_7_2))
        assertFalse(testFilter.isTestAllowed(thisTest, Xnat_1_7_3))
    }

    @Test
    @RequireXnatVersion(allowedVersions = [Xnat_1_7_2, Xnat_1_7_3])
    void disallowedClassAllowedMethod() {
        final IMethodInstance thisTest = UnitTestFilter.getInstance('disallowedClassAllowedMethod')

        assertTrue (testFilter.isTestAllowed(thisTest, Xnat_1_7_3))
        assertFalse(testFilter.isTestAllowed(thisTest, Xnat_1_7_2))
        assertFalse(testFilter.isTestAllowed(thisTest, Xnat_1_7_5))
        assertFalse(testFilter.isTestAllowed(thisTest, Xnat_1_6dev))
    }

}
