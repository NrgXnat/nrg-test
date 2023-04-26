package org.nrg.testing.listeners.adapters

import io.restassured.RestAssured
import groovy.util.logging.Log4j
import org.nrg.jira.reporter.JiraCompiler
import org.nrg.testing.BaseTestCase
import org.nrg.testing.FileIOUtils
import org.nrg.testing.TestController
import org.nrg.testing.TestNgUtils
import org.nrg.testing.annotations.HardDependency
import org.nrg.testing.annotations.PipelineCheckParams
import org.nrg.testing.email.SummaryEmail
import org.nrg.testing.jira.JIRASettings
import org.nrg.testing.listeners.adapters.jira.JIRATest
import org.nrg.testing.listeners.adapters.jira.JIRATestListener
import org.nrg.testing.listeners.adapters.jira.failure.FailureCause
import org.nrg.testing.util.TimeLog
import org.nrg.testing.xnat.BaseXnatTest
import org.nrg.testing.xnat.conf.Settings
import org.nrg.testing.xnat.rest.XnatRestDriver
import org.testng.ITestContext
import org.testng.ITestNGMethod
import org.testng.ITestResult

@Log4j
class NRGTestListener extends BaseTestListener {

    private TestController testController
    private XnatRestDriver xnatRestDriver
    private final Map<ITestNGMethod, FailureCause> failureReasons = [:]
    private final Map<ITestNGMethod, FailureCause> skipReasons = [:]
    private final TimeLog timeLog = new TimeLog()

    @Override
    void onConfigurationFailure(ITestResult itr) {
        log.warn("Configuration method ${itr.method.methodName} failed with stack trace:\n", itr.throwable)
        if (!testClassSupported(itr)) {
            return
        }
        super.onConfigurationFailure(itr)
        setFields(itr)
    }

    @Override
    void onStart(ITestResult result)  {
        if (!testClassSupported(result)) {
            return
        }
        setFields(result)
        if (Settings.CHECK_DEPENDENCIES) {
            final List<String> prerequisiteTests = []

            final HardDependency hardDependency = TestNgUtils.getAnnotation(result.method, HardDependency)
            if (hardDependency != null) {
                prerequisiteTests.addAll(hardDependency.value() as List<String>)
            }

            if (TestNgUtils.getAnnotation(result.method, PipelineCheckParams) != null) { // If it's a pipeline verification test...
                prerequisiteTests << testName.replace('Check', 'Launch')
            }

            if (!prerequisiteTests.isEmpty()) {
                final List<String> passed = extractPassedTests()
                prerequisiteTests.each { dependency ->
                    TestNgUtils.assumeTrue(dependency in passed, "Test ${testName} is being skipped because prerequisite test ${dependency} did not pass.")
                }
            }
        }
    }

    @Override
    void onFailure(ITestResult testResult)  {
        if (!testClassSupported(testResult)) {
            return
        }
        final JIRATest currentTest = testController.currentTest
        if (currentTest == null) {
            return
        }
        if (currentTest.failureReason == null) {
            try {
                RestAssured.given().get(Settings.BASEURL)
            } catch (Exception exception) {
                if (exception instanceof NoRouteToHostException || exception instanceof UnknownHostException) {
                    currentTest.setFailureReason("${exception.getClass().name}: test server seems to be inaccessible")
                }
            }
        }
        failureReasons.put(testResult.method, currentTest.failureReason)
        final String failedFiles = Settings.getFailedScreenshotPath(testClassName)

        final File exceptionLog = FileIOUtils.writeExceptionToFile(failedFiles, testName, testResult.throwable)
        if (exceptionLog != null) {
            currentTest.postStepAttachment(exceptionLog, testController.step)
            currentTest.postExecutionAttachment(exceptionLog)
        }

        testCleanup(testResult)
    }

    @Override
    void onSuccess(ITestResult testResult) {
        if (!testClassSupported(testResult)) {
            return
        }
        testCleanup(testResult)
    }

    @Override
    void onSkipped(ITestResult testResult) {
        if (!testClassSupported(testResult)) {
            return
        }
        if (testController.currentTest == null) {
            return
        }
        skipReasons.put(testResult.method, testController.currentTest.skipReason)

        testCleanup(testResult)
    }

    @Override
    void onTestComplete(ITestResult testResult) {
        if (!testClassSupported(testResult)) {
            return
        }
        setFields(testResult)
    }

    @Override
    void onFinish(ITestContext context) {
        if (Settings.TIMELOG_SETTING) {
            timeLog.writeTimeLogs()
        }
        if (Settings.PRODUCE_PDF) {
            try {
                new JiraCompiler(
                        JIRASettings.JIRA_URL,
                        JIRASettings.JIRA_USER,
                        JIRASettings.JIRA_PASS,
                        JIRASettings.PROJECT,
                        JIRASettings.VERSION,
                        JIRATestListener.cycle.name
                ).compileAndBuild(false)
            } catch (Exception e) {
                log.warn('JIRA PDF export failed due to: ', e)
            }
        }
        new SummaryEmail(getPassedTests(), failureReasons, skipReasons).sendSummaryEmail()
    }

    private boolean testClassSupported(ITestResult testResult) {
        testResult.instance instanceof BaseXnatTest
    }

    private void testCleanup(ITestResult testResult) {
        final JIRATest test = testController.currentTest
        if (test.jiraNumber != null) {
            test.postExecutionComment("Test ran for ${testController.currentTestTimer.time / 1000.0} seconds.")
        }
        if (Settings.TIMELOG_SETTING) {
            timeLog.addTimeLogEntry(testController.currentTestTimer, testResult)
        }
        testController.setTestRunning(false)
    }

    private void setFields(ITestResult testResult) {
        try {
            if (testResult.instance instanceof BaseXnatTest) {
                xnatRestDriver = (testResult.instance as BaseXnatTest).restDriver
                testController = (testResult.instance as BaseTestCase).testController
            }
        } catch (NullPointerException ignored) {
            final String nullEntity = (testResult == null) ? 'testResult' : 'testResult.instance'
            log.debug("Could not set driver in NRGTestListener due to NPE (in ${nullEntity})")
        }
    }

    private List<String> extractPassedTests() {
        getPassedTests().collect { result ->
            TestNgUtils.getTestName(result)
        }
    }

}