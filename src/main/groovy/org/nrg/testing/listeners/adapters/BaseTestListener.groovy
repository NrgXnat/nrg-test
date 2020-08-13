package org.nrg.testing.listeners.adapters

import org.nrg.testing.TestNgUtils
import org.testng.ITestResult
import org.testng.TestListenerAdapter

abstract class BaseTestListener extends TestListenerAdapter {

    Class testClass
    String testClassName
    String testName

    @Override
    final void onTestStart(ITestResult result) {
        super.onTestStart(result)
        extractSimpleTestInfo(result)
        onStart(result)
    }

    @Override
    final void onTestFailure(ITestResult testResult) {
        super.onTestFailure(testResult)
        extractSimpleTestInfo(testResult)
        onTestComplete(testResult)
        onFailure(testResult)
    }

    @Override
    final void onTestSuccess(ITestResult testResult) {
        super.onTestSuccess(testResult)
        extractSimpleTestInfo(testResult)
        onTestComplete(testResult)
        onSuccess(testResult)
    }

    @Override
    final void onTestSkipped(ITestResult testResult) {
        super.onTestSkipped(testResult)
        extractSimpleTestInfo(testResult)
        onTestComplete(testResult)
        onSkipped(testResult)
    }

    /**
     * Should run whenever a test completes (through one of onTestFailure(), onTestSuccess(), or onTestSkipped()
     * @param testResult Test having finished
     */
    abstract void onTestComplete(ITestResult testResult)

    abstract void onStart(ITestResult testResult)

    abstract void onFailure(ITestResult testResult)

    abstract void onSuccess(ITestResult testResult)

    abstract void onSkipped(ITestResult testResult)

    protected void extractSimpleTestInfo(ITestResult testResult) {
        testClass = TestNgUtils.getTestClass(testResult)
        testClassName = testClass.getSimpleName()
        testName = TestNgUtils.getTestName(testResult)
    }

}
