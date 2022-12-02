package org.nrg.testing.listeners.interceptors.filters

import org.nrg.testing.xnat.conf.Settings
import org.nrg.testing.xnat.performance.XnatPerformanceTests
import org.testng.IMethodInstance

class PerformanceTestFilter extends TestFilterInterceptor {

    PerformanceTestFilter() {
        super()
    }

    @Override
    boolean isTestAllowed(IMethodInstance testInstance) {
        !(XnatPerformanceTests.isAssignableFrom(testInstance.method.realClass)) || Settings.PERFORMANCE_TESTS_ALLOWED
    }

    @Override
    boolean isActive() {
        true
    }

}