package org.nrg.testing.tests;

import org.nrg.testing.UnitTestFilter;
import org.nrg.testing.annotations.DeprecatedIn;
import org.nrg.testing.listeners.interceptors.filters.ProhibitedTestFilter;
import org.nrg.testing.xnat.versions.*;
import org.testng.IMethodInstance;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

@SuppressWarnings("Duplicates")
@Listeners(UnitTestFilter.class)
@DeprecatedIn(Xnat_1_7_3.class)
public class DeprecatedInTest {

    private final ProhibitedTestFilter testFilter = new ProhibitedTestFilter();

    @Test
    public void deprecatedInTestClassLevel() {
        final IMethodInstance thisTest = UnitTestFilter.getInstance("deprecatedInTestClassLevel");

        assertFalse(testFilter.isTestAllowed(thisTest, Xnat_1_7dev.class));
        assertFalse(testFilter.isTestAllowed(thisTest, Xnat_1_7_5.class));
        assertFalse(testFilter.isTestAllowed(thisTest, Xnat_1_7_4.class));
        assertFalse(testFilter.isTestAllowed(thisTest, Xnat_1_7_3.class));
        assertTrue (testFilter.isTestAllowed(thisTest, Xnat_1_7_2.class));
        assertTrue (testFilter.isTestAllowed(thisTest, Xnat_1_6dev.class));
    }

    @Test
    @DeprecatedIn(Xnat_1_7_2.class)
    public void deprecatedInTestTwoLevels() {
        final IMethodInstance thisTest = UnitTestFilter.getInstance("deprecatedInTestTwoLevels");

        assertFalse(testFilter.isTestAllowed(thisTest, Xnat_1_7dev.class));
        assertFalse(testFilter.isTestAllowed(thisTest, Xnat_1_7_5.class));
        assertFalse(testFilter.isTestAllowed(thisTest, Xnat_1_7_4.class));
        assertFalse(testFilter.isTestAllowed(thisTest, Xnat_1_7_3.class));
        assertFalse(testFilter.isTestAllowed(thisTest, Xnat_1_7_2.class));
        assertTrue (testFilter.isTestAllowed(thisTest, Xnat_1_6dev.class));
    }

}
