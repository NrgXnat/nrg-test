package org.nrg.testing

import com.jayway.restassured.RestAssured
import groovy.util.logging.Log4j
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.mutable.MutableInt
import org.nrg.testing.annotations.JiraKey
import org.nrg.testing.jira.JIRASettings
import org.nrg.testing.listeners.adapters.NRGTestListener
import org.nrg.testing.listeners.adapters.git.GitLogListener
import org.nrg.testing.listeners.adapters.jira.JIRATestListener
import org.nrg.testing.listeners.interceptors.filters.ExpectedFailureTestFilter
import org.nrg.testing.listeners.interceptors.filters.PluginDependencyTestFilter
import org.nrg.testing.listeners.interceptors.sorters.DefaultMethodSorter
import org.nrg.testing.listeners.interceptors.filters.BasicTestFilter
import org.nrg.testing.listeners.interceptors.filters.ProhibitedTestFilter
import org.nrg.testing.listeners.interceptors.filters.TestFilterInterceptors
import org.nrg.testing.xnat.conf.Settings
import org.testng.ITestContext
import org.testng.ITestNGMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.BeforeSuite
import org.testng.annotations.Listeners

import java.lang.annotation.Annotation
import java.lang.reflect.Method
import java.nio.file.Paths

@Log4j
@Listeners([NRGTestListener, JIRATestListener, GitLogListener, DefaultMethodSorter, ProhibitedTestFilter, BasicTestFilter, ExpectedFailureTestFilter, PluginDependencyTestFilter])
class BaseTestCase {
    
    protected MutableInt stepCounter
    protected static Map<ITestNGMethod, String> allRunningTests
    protected TestController testController

    void deleteOldScreenshots() {
        [Settings.FAILED_SCREENSHOT_PATH, Settings.SCREENSHOT_PATH].each { path ->
            final File dir = new File(path)
            if (dir.exists()) {
                FileUtils.deleteDirectory(dir)
            }
        }
    }

    void setupJira() {
        if (Settings.JIRA_SETTING) {
            log.info('Setting up JIRA test integration...')
            JIRATestListener.init(JIRASettings.JIRA_URL, JIRASettings.PROJECT, JIRASettings.VERSION, JIRASettings.JIRA_CYCLE_NAME, JIRASettings.JIRA_USER, JIRASettings.JIRA_PASS, allRunningTests)
            JIRATestListener.updateEnvironmentInfo(Settings.BASEURL)
        } else {
            JIRATestListener.readJIRATests(allRunningTests) // even if JIRA management isn't used, we still piggyback off it for creating test objects and failure reasons
        }
    }

    @BeforeSuite
    void setupAllTests(ITestContext testContext) {
        RestAssured.useRelaxedHTTPSValidation()
        allRunningTests = constructTestMap(testContext.getAllTestMethods())
        log.info("${allRunningTests.size()} tests are scheduled to run.")
        setupJira()
        deleteOldScreenshots()
        new File(Settings.DATA_LOCATION).mkdir()
    }

    @BeforeMethod(alwaysRun = true)
    void setUpTest(Method m, ITestContext testContext) {
        final ITestNGMethod testNGMethod = getAllTests().find { test ->
            test.constructorOrMethod.method == m
        }

        testController = new TestController()
        stepCounter = testController.getStepCounter()

        setCurrentTestProperties(m)
        testController.selectJiraTest(testNGMethod)
        testController.setTestRunning(true)
        startTimers()
    }

    void setCurrentTestProperties(Method m) {
        testController.setTestClass(m.getDeclaringClass().getSimpleName())
        testController.setTestName(m.getName())
    }

    protected int numTestsToBeRunInClass(Class testClass) {
        getTestsByClass(testClass).size()
    }

    protected Map<ITestNGMethod, String> constructTestMap(ITestNGMethod[] tests) {
        tests.findAll { test ->
            TestFilterInterceptors.isTestAllowed(test)
        }.collectEntries { test ->
            [(test) : readJiraNumber(test)]
        }
    }

    protected String readJiraNumber(ITestNGMethod test) {
        final JiraKey key = TestNgUtils.getAnnotation(test, JiraKey)
        if (key != null) {
            final String simpleId = key.simpleKey()
            if (StringUtils.isNotEmpty(simpleId)) {
                simpleId
            } else {
                log.warn("When using @JiraKey annotation outside of XNAT tests, simpleKey() is required to mark JIRA number for tests. Value is empty on test: ${TestNgUtils.getTestName(test)}")
                null
            }
        } else {
            null
        }
    }

    protected void startTimers() {
        testController.startTestTimer()
    }

    protected boolean methodContainsAnnotation(Method method, Class<? extends Annotation> annotation) {
        method.getAnnotation(annotation) != null
    }

    protected List<ITestNGMethod> getTestsByClass(Class testClass) {
        if (allRunningTests == null) {
            log.fatal('allRunningTests object appears to be null, which makes no sense.')
            throw new RuntimeException()
        }
        allRunningTests.keySet().findAll { test ->
            TestNgUtils.getTestClass(test) == testClass
        } as List
    }

    protected File getDataFile(String filename) {
        Paths.get(Settings.DATA_LOCATION, filename).toFile()
    }

    protected String readDataFile(String filename) {
        getDataFile(filename).text
    }

    static Set<ITestNGMethod> getAllTests() {
        allRunningTests.keySet()
    }

    TestController getTestController() {
        testController
    }

}
