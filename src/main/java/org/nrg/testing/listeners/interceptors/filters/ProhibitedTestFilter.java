package org.nrg.testing.listeners.interceptors.filters;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.nrg.testing.annotations.AddedIn;
import org.nrg.testing.annotations.DeprecatedIn;
import org.nrg.testing.annotations.DisallowXnatVersion;
import org.nrg.testing.annotations.RequireXnatVersion;
import org.nrg.testing.util.TestNgUtils;
import org.nrg.testing.xnat.conf.Settings;
import org.nrg.testing.xnat.versions.XnatVersion;
import org.nrg.testing.xnat.versions.XnatVersionLineage;
import org.nrg.testing.xnat.versions.XnatVersionList;
import org.testng.IMethodInstance;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

import java.util.ArrayList;
import java.util.List;

public class ProhibitedTestFilter extends TestFilterInterceptor {

    private static final Logger LOGGER = Logger.getLogger(ProhibitedTestFilter.class);

    public ProhibitedTestFilter() {
        super();
        XnatVersionList.readXnatVersions();
    }

    @Override
    public boolean isTestAllowed(IMethodInstance testInstance) {
        return isTestAllowed(testInstance, Settings.XNAT_VERSION);
    }

    public boolean isTestAllowed(IMethodInstance testInstance, Class<? extends XnatVersion> versionClass) {
        final ITestNGMethod method = testInstance.getMethod();
        final Class<?> testClass = TestNgUtils.getTestClass(method);

        return !(
                violatesXnatVersionConstraint(testClass.getAnnotation(RequireXnatVersion.class), versionClass) ||
                violatesXnatVersionConstraint(testClass.getAnnotation(DisallowXnatVersion.class), versionClass) ||
                violatesXnatVersionConstraint(testClass.getAnnotation(AddedIn.class), versionClass) ||
                violatesXnatVersionConstraint(testClass.getAnnotation(DeprecatedIn.class), versionClass) ||
                violatesXnatVersionConstraint(TestNgUtils.getAnnotation(method, RequireXnatVersion.class), versionClass) ||
                violatesXnatVersionConstraint(TestNgUtils.getAnnotation(method, DisallowXnatVersion.class), versionClass) ||
                violatesXnatVersionConstraint(TestNgUtils.getAnnotation(method, AddedIn.class), versionClass) ||
                violatesXnatVersionConstraint(TestNgUtils.getAnnotation(method, DeprecatedIn.class), versionClass)
        );
    }

    private boolean violatesXnatVersionConstraint(RequireXnatVersion requireXnatVersion, Class<? extends XnatVersion> xnatVersion) {
        return requireXnatVersion != null && !ArrayUtils.contains(requireXnatVersion.allowedVersions(), xnatVersion);
    }

    private boolean violatesXnatVersionConstraint(DisallowXnatVersion disallowXnatVersion, Class<? extends XnatVersion> xnatVersion) {
        return disallowXnatVersion != null && ArrayUtils.contains(disallowXnatVersion.disallowedVersions(), xnatVersion);
    }

    private boolean violatesXnatVersionConstraint(AddedIn addedIn, Class<? extends XnatVersion> xnatVersion) {
        return addedIn != null && XnatVersionLineage.recursiveVersionSearch(addedIn.value(), xnatVersion);
    }

    private boolean violatesXnatVersionConstraint(DeprecatedIn deprecatedIn, Class<? extends XnatVersion> xnatVersion) {
        return deprecatedIn != null && !XnatVersionLineage.recursiveVersionSearch(deprecatedIn.value(), xnatVersion);
    }

    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
        final List<IMethodInstance> allowedTests = super.intercept(methods, context);
        final List<IMethodInstance> prohibitedTests = new ArrayList<>(methods);
        prohibitedTests.removeAll(allowedTests);

        if (prohibitedTests.isEmpty()) {
            LOGGER.info("No tests were filtered out by " + this.getClass().getSimpleName());
        } else {
            LOGGER.info("The following tests were filtered out and will not be executed: " + prohibitedTests);
        }

        return allowedTests;
    }

}