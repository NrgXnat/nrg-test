package org.nrg.testing.listeners.interceptors.filters;

import org.apache.log4j.Logger;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;

import java.util.ArrayList;
import java.util.List;

public abstract class TestFilterInterceptor implements IMethodInterceptor {

    private static final Logger LOGGER = Logger.getLogger(TestFilterInterceptor.class);

    public TestFilterInterceptor() {
        TestFilterInterceptors.registerListener(this);
    }

    public abstract boolean isTestAllowed(IMethodInstance testInstance);

    public abstract boolean isActive();

    @Override
    public final List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
        Logger.getLogger(this.getClass()).debug("Method instance filtering intercepted in " + this.getClass().getSimpleName());
        if (!isActive()) {
            LOGGER.info(this.getClass().getSimpleName() + " is not active. No tests will be filtered out by this class.");
            return methods;
        }
        final List<IMethodInstance> allowedTests = new ArrayList<>();
        final List<IMethodInstance> prohibitedTests = new ArrayList<>();

        for (IMethodInstance methodInstance : methods) {
            (isTestAllowed(methodInstance) ? allowedTests : prohibitedTests).add(methodInstance);
        }

        if (prohibitedTests.isEmpty()) {
            LOGGER.info("No tests were filtered out by " + this.getClass().getSimpleName());
        } else {
            LOGGER.info("The following tests were filtered out and will not be executed: " + prohibitedTests);
        }

        return allowedTests;
    }

}
