package org.nrg.testing.tests

import org.nrg.testing.unit.UnitTestFilter
import org.nrg.testing.annotations.DeprecatedIn
import org.nrg.testing.listeners.interceptors.filters.ProhibitedTestFilter
import org.nrg.xnat.versions.*
import org.testng.IMethodInstance
import org.testng.annotations.Listeners
import org.testng.annotations.Test

import static org.testng.AssertJUnit.assertFalse
import static org.testng.AssertJUnit.assertTrue

@SuppressWarnings('Duplicates')
@Listeners(UnitTestFilter)
@DeprecatedIn(Xnat_1_7_3)
class DeprecatedInTest {

    private final ProhibitedTestFilter testFilter = new ProhibitedTestFilter()

    @Test
    void deprecatedInTestClassLevel() {
        final IMethodInstance thisTest = UnitTestFilter.getInstance('deprecatedInTestClassLevel')

        assertFalse(testFilter.isTestAllowed(thisTest, Xnat_1_7_5))
        assertFalse(testFilter.isTestAllowed(thisTest, Xnat_1_7_4))
        assertFalse(testFilter.isTestAllowed(thisTest, Xnat_1_7_3))
        assertTrue (testFilter.isTestAllowed(thisTest, Xnat_1_7_2))
        assertTrue (testFilter.isTestAllowed(thisTest, Xnat_1_6dev))
    }

    @Test
    @DeprecatedIn(Xnat_1_7_2.class)
    void deprecatedInTestTwoLevels() {
        final IMethodInstance thisTest = UnitTestFilter.getInstance('deprecatedInTestTwoLevels')

        assertFalse(testFilter.isTestAllowed(thisTest, Xnat_1_7_5))
        assertFalse(testFilter.isTestAllowed(thisTest, Xnat_1_7_4))
        assertFalse(testFilter.isTestAllowed(thisTest, Xnat_1_7_3))
        assertFalse(testFilter.isTestAllowed(thisTest, Xnat_1_7_2))
        assertTrue (testFilter.isTestAllowed(thisTest, Xnat_1_6dev))
    }

}
