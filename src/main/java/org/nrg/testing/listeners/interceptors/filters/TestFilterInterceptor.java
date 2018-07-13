package org.nrg.testing.listeners.interceptors.filters;

import org.apache.log4j.Logger;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;

import java.util.ArrayList;
import java.util.List;

public abstract class TestFilterInterceptor implements IMethodInterceptor {

    public abstract boolean isTestAllowed(IMethodInstance testInstance);

    public TestFilterInterceptor() {
        TestFilterInterceptors.registerListener(this);
    }

    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
        Logger.getLogger(this.getClass()).debug("Method instance filtering intercepted in " + this.getClass().getSimpleName());
        final List<IMethodInstance> allowedTests = new ArrayList<>();

        for (IMethodInstance methodInstance : methods) {
            if (isTestAllowed(methodInstance)) {
                allowedTests.add(methodInstance);
            }
        }
        return allowedTests;
    }

}
