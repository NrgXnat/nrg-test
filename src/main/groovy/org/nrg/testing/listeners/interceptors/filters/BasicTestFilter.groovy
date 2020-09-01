package org.nrg.testing.listeners.interceptors.filters

import org.nrg.testing.annotations.Basic
import org.nrg.testing.xnat.conf.Settings
import org.testng.IMethodInstance

class BasicTestFilter extends TestFilterInterceptor {

    BasicTestFilter() {
        super()
    }

    @Override
    boolean isTestAllowed(IMethodInstance testInstance) {
        testOrClassHasAnnotation(testInstance, Basic)
    }

    @Override
    boolean isActive() {
        Settings.BASIC_MODE
    }

}