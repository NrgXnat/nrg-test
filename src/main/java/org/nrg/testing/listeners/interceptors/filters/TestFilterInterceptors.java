package org.nrg.testing.listeners.interceptors.filters;

import org.testng.IMethodInstance;
import org.testng.ITestNGMethod;
import org.testng.internal.MethodInstance;

import java.util.ArrayList;
import java.util.List;

public class TestFilterInterceptors {

    private static final List<TestFilterInterceptor> interceptors = new ArrayList<>();

    public static void registerListener(TestFilterInterceptor interceptor) {
        interceptors.add(interceptor);
    }

    public static boolean isTestAllowed(IMethodInstance testInstance) {
        for (TestFilterInterceptor interceptor : interceptors) {
            if (interceptor.isActive() && !interceptor.isTestAllowed(testInstance)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isTestAllowed(ITestNGMethod testNGMethod) {
        return isTestAllowed(new MethodInstance(testNGMethod));
    }

}
