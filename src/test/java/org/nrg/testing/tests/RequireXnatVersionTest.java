package org.nrg.testing.tests;

import org.nrg.testing.UnitTestFilter;
import org.nrg.testing.annotations.DisallowXnatVersion;
import org.nrg.testing.annotations.RequireXnatVersion;
import org.nrg.testing.listeners.interceptors.filters.ProhibitedTestFilter;
import org.nrg.testing.xnat.versions.Xnat_1_6dev;
import org.nrg.testing.xnat.versions.Xnat_1_7_2;
import org.nrg.testing.xnat.versions.Xnat_1_7_3;
import org.nrg.testing.xnat.versions.Xnat_1_7dev;
import org.testng.IMethodInstance;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.*;

@Listeners(UnitTestFilter.class)
@RequireXnatVersion(allowedVersions = {Xnat_1_6dev.class, Xnat_1_7_2.class})
public class RequireXnatVersionTest {

    private final ProhibitedTestFilter testFilter = new ProhibitedTestFilter();

    @Test
    @RequireXnatVersion(allowedVersions = Xnat_1_6dev.class)
    public void requiredClassAndMethod() {
        final IMethodInstance thisTest = UnitTestFilter.getInstance("requiredClassAndMethod");

        assertTrue (testFilter.isTestAllowed(thisTest, Xnat_1_6dev.class));
        assertFalse(testFilter.isTestAllowed(thisTest, Xnat_1_7_2.class));
        assertFalse(testFilter.isTestAllowed(thisTest, Xnat_1_7dev.class));
    }

    @Test
    @DisallowXnatVersion(disallowedVersions = {Xnat_1_7_2.class})
    public void requiredClassDisallowedMethod() {
        final IMethodInstance thisTest = UnitTestFilter.getInstance("requiredClassDisallowedMethod");

        assertTrue (testFilter.isTestAllowed(thisTest, Xnat_1_6dev.class));
        assertFalse(testFilter.isTestAllowed(thisTest, Xnat_1_7_2.class));
        assertFalse(testFilter.isTestAllowed(thisTest, Xnat_1_7_3.class));
    }

}
