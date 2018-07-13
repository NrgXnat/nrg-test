package org.nrg.testing.tests;

import org.nrg.testing.UnitTestFilter;
import org.nrg.testing.annotations.AddedIn;
import org.nrg.testing.annotations.DisallowXnatVersion;
import org.nrg.testing.annotations.RequireXnatVersion;
import org.nrg.testing.listeners.interceptors.filters.ProhibitedTestFilter;
import org.nrg.testing.xnat.versions.*;
import org.testng.IMethodInstance;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

@SuppressWarnings("Duplicates")
@Listeners(UnitTestFilter.class)
@AddedIn(Xnat_1_7_2.class)
public class AddedInTest {

    private final ProhibitedTestFilter testFilter = new ProhibitedTestFilter();

    @Test
    public void addedInTestClassLevel() {
        final IMethodInstance thisTest = UnitTestFilter.getInstance("addedInTestClassLevel");

        assertTrue (testFilter.isTestAllowed(thisTest, Xnat_1_7dev.class));
        assertTrue (testFilter.isTestAllowed(thisTest, Xnat_1_7_5.class));
        assertTrue (testFilter.isTestAllowed(thisTest, Xnat_1_7_4.class));
        assertTrue (testFilter.isTestAllowed(thisTest, Xnat_1_7_3.class));
        assertTrue (testFilter.isTestAllowed(thisTest, Xnat_1_7_2.class));
        assertFalse(testFilter.isTestAllowed(thisTest, Xnat_1_6dev.class));
    }

    @Test
    @AddedIn(Xnat_1_7_3.class)
    public void addedInTestTwoLevels() {
        final IMethodInstance thisTest = UnitTestFilter.getInstance("addedInTestTwoLevels");

        assertTrue (testFilter.isTestAllowed(thisTest, Xnat_1_7dev.class));
        assertTrue (testFilter.isTestAllowed(thisTest, Xnat_1_7_5.class));
        assertTrue (testFilter.isTestAllowed(thisTest, Xnat_1_7_4.class));
        assertTrue (testFilter.isTestAllowed(thisTest, Xnat_1_7_3.class));
        assertFalse(testFilter.isTestAllowed(thisTest, Xnat_1_7_2.class));
        assertFalse(testFilter.isTestAllowed(thisTest, Xnat_1_6dev.class));
    }

}
