package org.nrg.testing.tests

import org.nrg.testing.UnitTestFilter
import org.nrg.testing.annotations.AddedIn
import org.nrg.testing.listeners.interceptors.filters.ProhibitedTestFilter
import org.nrg.testing.xnat.versions.*
import org.testng.IMethodInstance
import org.testng.annotations.Listeners
import org.testng.annotations.Test

import static org.testng.AssertJUnit.assertFalse
import static org.testng.AssertJUnit.assertTrue

@SuppressWarnings('Duplicates')
@Listeners(UnitTestFilter)
@AddedIn(Xnat_1_7_2)
class AddedInTest {

    private final ProhibitedTestFilter testFilter = new ProhibitedTestFilter()

    @Test
    void addedInTestClassLevel() {
        final IMethodInstance thisTest = UnitTestFilter.getInstance('addedInTestClassLevel')

        assertTrue (testFilter.isTestAllowed(thisTest, Xnat_1_7dev))
        assertTrue (testFilter.isTestAllowed(thisTest, Xnat_1_7_5))
        assertTrue (testFilter.isTestAllowed(thisTest, Xnat_1_7_4))
        assertTrue (testFilter.isTestAllowed(thisTest, Xnat_1_7_3))
        assertTrue (testFilter.isTestAllowed(thisTest, Xnat_1_7_2))
        assertFalse(testFilter.isTestAllowed(thisTest, Xnat_1_6dev))
    }

    @Test
    @AddedIn(Xnat_1_7_3)
    void addedInTestTwoLevels() {
        final IMethodInstance thisTest = UnitTestFilter.getInstance('addedInTestTwoLevels')

        assertTrue (testFilter.isTestAllowed(thisTest, Xnat_1_7dev))
        assertTrue (testFilter.isTestAllowed(thisTest, Xnat_1_7_5))
        assertTrue (testFilter.isTestAllowed(thisTest, Xnat_1_7_4))
        assertTrue (testFilter.isTestAllowed(thisTest, Xnat_1_7_3))
        assertFalse(testFilter.isTestAllowed(thisTest, Xnat_1_7_2))
        assertFalse(testFilter.isTestAllowed(thisTest, Xnat_1_6dev))
    }

}
