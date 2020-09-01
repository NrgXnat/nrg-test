package org.nrg.testing.listeners.interceptors.filters

import org.nrg.testing.annotations.ExpectedFailure
import org.nrg.testing.enums.TestBehavior
import org.nrg.testing.xnat.conf.Settings
import org.testng.IMethodInstance

class ExpectedFailureTestFilter extends TestFilterInterceptor {

    ExpectedFailureTestFilter() {
        super()
    }

    @Override
    boolean isTestAllowed(IMethodInstance testInstance) {
        !testOrClassHasAnnotation(testInstance, ExpectedFailure)
    }

    @Override
    boolean isActive() {
        Settings.BEHAVIOR_FOR_EXPECTED_FAILURES == TestBehavior.IGNORE
    }

}