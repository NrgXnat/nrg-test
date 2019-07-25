package org.nrg.testing.listeners.interceptors.filters

import org.testng.IMethodInstance
import org.testng.ITestNGMethod
import org.testng.internal.MethodInstance

class TestFilterInterceptors {

    private static final List<TestFilterInterceptor> interceptors = []

    static void registerListener(TestFilterInterceptor interceptor) {
        interceptors << interceptor
    }

    static boolean isTestAllowed(IMethodInstance testInstance) {
        !interceptors.any { interceptor ->
            interceptor.isActive() && !interceptor.isTestAllowed(testInstance)
        }
    }

    static boolean isTestAllowed(ITestNGMethod testNGMethod) {
        isTestAllowed(new MethodInstance(testNGMethod))
    }

}
