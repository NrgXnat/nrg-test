package org.nrg.testing.listeners.interceptors.filters

import org.nrg.testing.TestNgUtils
import org.nrg.testing.annotations.Basic
import org.nrg.testing.xnat.conf.Settings
import org.testng.IMethodInstance

class BasicTestFilter extends TestFilterInterceptor {

    BasicTestFilter() {
        if (isActive()) {
            TestFilterInterceptors.registerListener(this)
        }
    }

    @Override
    boolean isTestAllowed(IMethodInstance testInstance) {
        final Class<?> classObj = testInstance.method.realClass
        TestNgUtils.getAnnotation(testInstance.method, Basic) != null || classObj.getAnnotation(Basic) != null
    }

    @Override
    boolean isActive() {
        Settings.BASIC_MODE
    }

}