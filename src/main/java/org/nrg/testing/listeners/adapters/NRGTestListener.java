package org.nrg.testing.listeners.adapters;

import com.jayway.restassured.RestAssured;
import org.apache.log4j.Logger;
import org.nrg.jira_reporter.JiraReporter;
import org.nrg.listeners.BaseListener;
import org.nrg.listeners.jira.JIRATest;
import org.nrg.listeners.jira.JIRATestNGListener;
import org.nrg.listeners.jira.failure.FailureCause;
import org.nrg.testing.BaseTestCase;
import org.nrg.testing.TestController;
import org.nrg.testing.annotations.HardDependency;
import org.nrg.testing.annotations.PipelineCheckParams;
import org.nrg.testing.email.SummaryEmail;
import org.nrg.testing.file.FileIO;
import org.nrg.testing.util.TestNgUtils;
import org.nrg.testing.util.TimeLog;
import org.nrg.testing.xnat.BaseXnatTest;
import org.nrg.testing.xnat.conf.Settings;
import org.nrg.testing.xnat.rest.XnatRestDriver;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

import java.io.File;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.util.*;

import static org.nrg.testing.jira.JIRASettings.*;

public class NRGTestListener extends BaseListener {

    private static final Logger LOGGER = Logger.getLogger(NRGTestListener.class);
    private TestController testController;
    private XnatRestDriver xnatRestDriver;
    private final Map<ITestNGMethod, FailureCause> failureReasons = new LinkedHashMap<>();

    @Override
    public void onConfigurationFailure(ITestResult itr) {
        super.onConfigurationFailure(itr);
        LOGGER.warn(String.format("Configuration method %s failed with stack trace:\n", itr.getMethod().getMethodName()), itr.getThrowable());
        setFields(itr);
    }

    @Override
    public void onTestStart(ITestResult result)  {
        super.onTestStart(result);
        setFields(result);
        if (Settings.CHECK_DEPENDENCIES) {
            final HardDependency hardDependency = TestNgUtils.getAnnotation(result.getMethod(), HardDependency.class);
            final List<String> prerequisiteTests = new ArrayList<>();

            if (hardDependency != null) {
                prerequisiteTests.addAll(Arrays.asList(hardDependency.value()));
            }

            if (TestNgUtils.getAnnotation(result.getMethod(), PipelineCheckParams.class) != null) { // If it's a pipeline verification test...
                prerequisiteTests.add(testName.replace("Check", "Launch"));
            }

            for (String dependency : prerequisiteTests) {
                TestNgUtils.assumeTrue(extractPassedTests().contains(dependency), String.format("Test %s is being skipped because prerequisite test %s did not pass.", testName, dependency));
            }
        }
    }

    @Override
    public void onTestFailure(ITestResult testResult)  {
        super.onTestFailure(testResult);
        setFields(testResult);
        final JIRATest currentTest = testController.getCurrentTest();
        if (currentTest.getFailureReason() == null) {
            try {
                RestAssured.given().get(Settings.BASEURL);
            } catch (Exception exception) {
                // noinspection ConstantConditions
                if (exception instanceof NoRouteToHostException || exception instanceof UnknownHostException) {
                    currentTest.setFailureReason(String.format("%s: test server seems to be inaccessible", exception.getClass().getName()));
                }
            }
        }
        failureReasons.put(testResult.getMethod(), currentTest.getFailureReason());
        final String failedFiles = Settings.getFailedScreenshotPath(testClassName);

        final File exception = FileIO.writeExceptionToFile(failedFiles, testName, testResult.getThrowable());
        if (exception != null) {
            currentTest.postStepAttachment(exception, testController.getStep());
            currentTest.postExecutionAttachment(exception);
        }

        testCleanup(testResult);
    }

    @Override
    public void onTestSuccess(ITestResult testResult) {
        super.onTestSuccess(testResult);

        setFields(testResult);
        testCleanup(testResult);
    }

    @Override
    public void onTestSkipped(ITestResult testResult) {
        super.onTestSkipped(testResult);

        setFields(testResult);
        testCleanup(testResult);
    }

    @Override
    public void onTestComplete(ITestResult testResult) {
        setFields(testResult);
    }

    @Override
    public void onFinish(ITestContext context) {
        if (Settings.TIMELOG_SETTING) {
            TimeLog.writeTimeLogs();
        }
        if (Settings.PRODUCE_PDF) {
            try {
                new JiraReporter(JIRA_URL, JIRA_USER, JIRA_PASS, PROJECT, VERSION, JIRATestNGListener.getCycle().getName()).compile();
            } catch (Exception e) {
                LOGGER.warn("JIRA PDF export failed due to:", e);
            }
        }
        SummaryEmail.sendSummaryEmail(xnatRestDriver, getPassedTests(), failureReasons, getSkippedTests());
    }

    private void testCleanup(ITestResult testResult) {
        final JIRATest test = testController.getCurrentTest();
        if (test.getJiraNumber() != null) test.postExecutionComment(String.format("Test ran for %s seconds.", testController.getCurrentTestTimer().getTime()/1000.0));
        if (Settings.TIMELOG_SETTING) {
            TimeLog.addTimeLogEntry(testController.getCurrentTestTimer(), testResult);
        }
        testController.setTestRunning(false);
    }

    private void setFields(ITestResult testResult) {
        try {
            if (testResult.getInstance() instanceof BaseXnatTest) xnatRestDriver = ((BaseXnatTest)testResult.getInstance()).getRestDriver();
            testController = ((BaseTestCase)testResult.getInstance()).getTestController();
        } catch (NullPointerException npe) {
            String nullEntity;
            if (testResult == null) nullEntity = "testResult";
            else nullEntity = "testResult.getInstance()";
            LOGGER.debug(String.format("Could not set driver in NRGTestListener due to NPE (in %s)", nullEntity));
        }
    }

    private List<String> extractPassedTests() {
        final List<String> passing = new ArrayList<>();
        for (ITestResult result : getPassedTests()) {
            passing.add(TestNgUtils.getTestName(result));
        }
        return passing;
    }

}