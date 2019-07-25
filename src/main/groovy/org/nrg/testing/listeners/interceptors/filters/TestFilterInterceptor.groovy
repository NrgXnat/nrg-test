package org.nrg.testing.listeners.interceptors.filters

import org.apache.log4j.Logger
import org.testng.IMethodInstance
import org.testng.IMethodInterceptor
import org.testng.ITestContext

abstract class TestFilterInterceptor implements IMethodInterceptor {

    private static final Logger LOGGER = Logger.getLogger(TestFilterInterceptor)

    TestFilterInterceptor() {
        TestFilterInterceptors.registerListener(this)
    }

    abstract boolean isTestAllowed(IMethodInstance testInstance)

    abstract boolean isActive()

    @Override
    final List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
        final String className = this.getClass().simpleName
        LOGGER.debug("Method instance filtering intercepted in ${className}")
        if (!isActive()) {
            LOGGER.info("${className} is not active. No tests will be filtered out by this class.")
            return methods
        }
        final List<IMethodInstance> allowedTests
        final List<IMethodInstance> prohibitedTests

        (allowedTests, prohibitedTests) = methods.split { methodInstance ->
            isTestAllowed(methodInstance)
        }

        if (prohibitedTests.isEmpty()) {
            LOGGER.info("No tests were filtered out by ${className}")
        } else {
            LOGGER.info("The following tests were filtered out and will not be executed: ${prohibitedTests}")
        }

        allowedTests
    }

}
