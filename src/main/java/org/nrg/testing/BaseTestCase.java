package org.nrg.testing;

import com.jayway.restassured.RestAssured;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.log4j.Logger;
import org.nrg.listeners.git.GitLogListener;
import org.nrg.listeners.jira.JIRATestNGListener;
import org.nrg.testing.annotations.JiraKey;
import org.nrg.testing.file.FileIO;
import org.nrg.testing.jira.JIRASettings;
import org.nrg.testing.listeners.adapters.NRGTestListener;
import org.nrg.testing.listeners.interceptors.sorters.DefaultMethodSorter;
import org.nrg.testing.listeners.interceptors.filters.BasicTestFilter;
import org.nrg.testing.listeners.interceptors.filters.ProhibitedTestFilter;
import org.nrg.testing.listeners.interceptors.filters.TestFilterInterceptors;
import org.nrg.testing.util.TestNgUtils;
import org.nrg.testing.xnat.conf.Settings;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

@Listeners({NRGTestListener.class, JIRATestNGListener.class, GitLogListener.class, DefaultMethodSorter.class, ProhibitedTestFilter.class, BasicTestFilter.class})
public class BaseTestCase {

    protected static final Logger LOGGER = Logger.getLogger(BaseTestCase.class);
    protected MutableInt stepCounter;
    protected static Map<ITestNGMethod, String> allRunningTests;
    protected TestController testController;

    public void deleteOldScreenshots() {
        FileIO.rmdir(Settings.FAILED_SCREENSHOT_PATH);
        FileIO.rmdir(Settings.SCREENSHOT_PATH);
    }

    public void setupJira() {
        if (Settings.JIRA_SETTING) {
            LOGGER.info("Setting up JIRA test integration...");
            JIRATestNGListener.init(JIRASettings.JIRA_URL, JIRASettings.PROJECT, JIRASettings.VERSION, JIRASettings.JIRA_CYCLE_NAME, JIRASettings.JIRA_USER, JIRASettings.JIRA_PASS, allRunningTests);
            JIRATestNGListener.updateEnvironmentInfo(Settings.BASEURL);
        } else {
            JIRATestNGListener.readJIRATests(allRunningTests); // even if JIRA management isn't used, we still piggyback off it for creating test objects and failure reasons
        }
    }

    @BeforeSuite
    public void setupAllTests(ITestContext testContext) {
        RestAssured.useRelaxedHTTPSValidation();
        allRunningTests = constructTestMap(testContext.getAllTestMethods());
        LOGGER.info(allRunningTests.size() + " tests are scheduled to run.");
        setupJira();
        deleteOldScreenshots();
        new File(Settings.DATA_LOCATION).mkdir();
    }

    @BeforeMethod(alwaysRun = true)
    public void setUpTest(Method m, ITestContext testContext) {
        final ITestNGMethod testNGMethod = TestNgUtils.getTestByMethod(m);

        testController = new TestController();
        stepCounter = testController.getStepCounter();

        setCurrentTestProperties(m);
        testController.selectJiraTest(testNGMethod);
        testController.setTestRunning(true);
        startTimers();
    }

    public void setCurrentTestProperties(Method m) {
        testController.setTestClass(m.getDeclaringClass().getSimpleName());
        testController.setTestName(m.getName());
    }

    protected int numTestsToBeRunInClass(Class testClass) {
        return getTestsByClass(testClass).size();
    }

    protected Map<ITestNGMethod, String> constructTestMap(ITestNGMethod[] tests) {
        final Map<ITestNGMethod, String> runningTests = new HashMap<>();

        for (ITestNGMethod test : tests) {
            if (TestFilterInterceptors.isTestAllowed(test)) {
                runningTests.put(test, readJiraNumber(test));
            }
        }

        return runningTests;
    }

    protected String readJiraNumber(ITestNGMethod test) {
        final JiraKey key = TestNgUtils.getAnnotation(test, JiraKey.class);
        if (key != null) {
            final String simpleId = key.simpleKey();

            if (StringUtils.isNotEmpty(simpleId)) {
                return simpleId;
            } else {
                LOGGER.warn("When using @JiraKey annotation outside of XNAT tests, simpleKey() is required to mark JIRA number for tests. Value is empty on test: " + TestNgUtils.getTestName(test));
                return null;
            }
        } else {
            return null;
        }
    }

    protected void startTimers() {
        testController.startTestTimer();
    }

    protected boolean methodContainsAnnotation(Method method, Class<? extends Annotation> annotation) {
        return method.getAnnotation(annotation) != null;
    }

    protected List<ITestNGMethod> getTestsByClass(Class testClass) {
        final List<ITestNGMethod> classTests = new ArrayList<>();
        if (allRunningTests == null) {
            LOGGER.fatal("allRunningTests object appears to be null, which makes no sense.");
            throw new RuntimeException();
        }
        for (ITestNGMethod test : allRunningTests.keySet()) {
            if (TestNgUtils.getTestClass(test).equals(testClass)) classTests.add(test);
        }
        return classTests;
    }

    public static Set<ITestNGMethod> getAllTests() {
        return allRunningTests.keySet();
    }

    public TestController getTestController() {
        return testController;
    }
}
