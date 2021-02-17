package org.nrg.testing.listeners.interceptors.filters

import org.nrg.testing.TestNgUtils
import org.nrg.testing.annotations.AddedIn
import org.nrg.testing.annotations.DeprecatedIn
import org.nrg.testing.annotations.DisallowXnatVersion
import org.nrg.testing.annotations.RequireXnatVersion
import org.nrg.testing.xnat.conf.Settings
import org.nrg.xnat.versions.XnatVersion
import org.nrg.xnat.versions.XnatVersionList
import org.testng.IMethodInstance
import org.testng.ITestNGMethod

import java.lang.annotation.Annotation

class ProhibitedTestFilter extends TestFilterInterceptor {

    ProhibitedTestFilter() {
        super()
    }

    @Override
    boolean isTestAllowed(IMethodInstance testInstance) {
        isTestAllowed(testInstance, Settings.XNAT_VERSION)
    }

    @Override
    boolean isActive() {
        true
    }

    boolean isTestAllowed(IMethodInstance testInstance, Class<? extends XnatVersion> versionClass) {
        final ITestNGMethod method = testInstance.method
        final Class<?> testClass = TestNgUtils.getTestClass(method)

        return ([RequireXnatVersion, DisallowXnatVersion, AddedIn, DeprecatedIn] as List<Class<? extends Annotation>>).every { annotationClass ->
            !(
                    violatesXnatVersionConstraint(testClass.getAnnotation(annotationClass), versionClass) ||
                    violatesXnatVersionConstraint(TestNgUtils.getAnnotation(method, annotationClass), versionClass)
            )
        }
    }

    private boolean violatesXnatVersionConstraint(Annotation annotation, Class <? extends XnatVersion> xnatVersion) {
        if (annotation == null) {
            false
        } else {
            switch (annotation.class) {
                case (RequireXnatVersion) :
                    return !(xnatVersion in (annotation as RequireXnatVersion).allowedVersions())
                case (DisallowXnatVersion) :
                    return xnatVersion in (annotation as DisallowXnatVersion).disallowedVersions()
                case (AddedIn) :
                    return XnatVersionList.firstFollowsSecond((annotation as AddedIn).value(), xnatVersion)
                case (DeprecatedIn) :
                    return !XnatVersionList.firstFollowsSecond((annotation as DeprecatedIn).value(), xnatVersion)
                default :
                    return true
            }
        }
    }

}