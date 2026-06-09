package org.nrg.testing.listeners.adapters

import org.nrg.testing.TestNgUtils
import org.testng.ITestResult
import org.testng.TestListenerAdapter

abstract class BaseTestListener extends TestListenerAdapter {

    // ThreadLocal so that concurrent test classes (TestNG parallel="classes") each see their own
    // test's identity; the getters preserve the original property API for subclasses.
    private final ThreadLocal<Class> currentTestClass = new ThreadLocal<>()
    private final ThreadLocal<String> currentTestName = new ThreadLocal<>()

    Class getTestClass() {
        currentTestClass.get()
    }

    String getTestClassName() {
        currentTestClass.get()?.simpleName
    }

    String getTestName() {
        currentTestName.get()
    }

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
        currentTestClass.set(TestNgUtils.getTestClass(testResult))
        currentTestName.set(TestNgUtils.getTestName(testResult))
    }

}
