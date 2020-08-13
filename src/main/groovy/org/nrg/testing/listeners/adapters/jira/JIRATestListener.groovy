package org.nrg.testing.listeners.adapters.jira

import org.apache.log4j.Logger
import org.nrg.jira.JiraZephyrController
import org.nrg.jira.components.zephyr.Cycle
import org.nrg.jira.components.zephyr.TestStatus
import org.nrg.jira.exceptions.JiraZephyrException
import org.nrg.testing.TestNgUtils
import org.nrg.testing.TimeUtils
import org.nrg.testing.listeners.adapters.BaseTestListener
import org.testng.ITestNGMethod
import org.testng.ITestResult

class JIRATestListener extends BaseTestListener {

    private static final Logger LOGGER = Logger.getLogger(JIRATestListener)
    private String currentTestNumber
    private JIRATest currentTest
    private static boolean stepAttachmentsAllowed = true
    private static Cycle cycle
    private static JiraZephyrController jiraZephyrController
    private static final Map<ITestNGMethod, JIRATest> jiraTests = [:]

    @Override
    void onStart(ITestResult result)  {
        getJiraTest(result.method).updateExecutionStatus(TestStatus.WIP)
        LOGGER.info("Starting test: ${testName}")
    }

    @Override
    void onFailure(ITestResult testResult)  {
        currentTest.failTest()
        LOGGER.warn(testLoggingMessage('failed'))
    }

    @Override
    void onSuccess(ITestResult testResult) {
        currentTest.passTest()
        LOGGER.info(testLoggingMessage('passed'))
    }

    @Override
    void onSkipped(ITestResult testResult) {
        currentTest.skipTest()
        LOGGER.info(testLoggingMessage('skipped'))
    }

    @Override
    void onTestComplete(ITestResult testResult) {
        currentTest = getJiraTest(testResult.getMethod())
        currentTestNumber = currentTest.getJiraNumber()
    }

    private String testLoggingMessage(String verb) {
        final String appendedNumber = (currentTestNumber == null) ? '' : " (${currentTestNumber})"
        "Test ${verb}: ${testName}${appendedNumber}."
    }

    /**
     * Creates cycle and initializes JIRA tests
     * @param jiraUrl URL for your JIRA
     * @param jiraProject Project that contains the tests
     * @param fixVersion Fix version to associate tests to
     * @param cycleName Name of the cycle to create
     * @param jiraUser Username for authentication
     * @param jiraPassword Password for authentication
     * @param allRunningTests Map of test objects to their corresponding JIRA number (e.g. XNAT-101). All tests <b>MUST</b> be included in this list, but if no JIRA test is available, the test object can be mapped to null
     */
    static void init(String jiraUrl, String jiraProject, String fixVersion, String cycleName, String jiraUser, String jiraPassword, Map<ITestNGMethod, String> allRunningTests) {
        createCycle(jiraUrl, jiraProject, fixVersion, cycleName, jiraUser, jiraPassword)
        readJIRATests(allRunningTests)
    }

    static void createCycle(String jiraUrl, String jiraProject, String fixVersion, String cycleName, String jiraUser, String jiraPassword) {
        try {
            jiraZephyrController = new JiraZephyrController(jiraUrl, jiraUser, jiraPassword)
            cycle = jiraZephyrController.createCycle("${cycleName}: ${TimeUtils.getTimestamp()}", jiraProject, fixVersion)
        } catch (JiraZephyrException e) {
            LOGGER.warn('Error occurred in creating cycle in JIRA.', e)
        }
    }

    /**
     * Reads in map to populate JIRA test objects
     * @param testMap Map from ITestNGMethod objects to their JIRA test number (e.g. XNAT-101). Any test to be run without an ID should still be included in the map (mapped to null).
     */
    static void readJIRATests(Map<ITestNGMethod, String> testMap) {
        jiraTests.clear()
        testMap.each { test, testNumber ->
            final String testName = TestNgUtils.getTestName(test)
            final String testClass = TestNgUtils.getTestClassName(test)
            if (cycle != null && testNumber != null) { // If setup has been called (JIRA testing is enabled) and test has a corresponding JIRA entry
                final JIRATest jiraTest = new JIRATest(jiraZephyrController, cycle, testNumber, testNumber, testClass)
                jiraTest.createExecution()
                jiraTests.put(test, jiraTest)
            } else { // Handle case of either no JIRA integration, or test doesn't have ID
                jiraTests.put(test, new NonInteractiveTest(testName, testClass))
            }
        }
    }

    static JIRATest getJiraTest(ITestNGMethod test) {
        jiraTests.get(test)
    }

    static void updateEnvironmentInfo(String environment) {
        try {
            jiraZephyrController.updateEnvironmentInfo(cycle, environment)
        } catch (Exception e) {
            LOGGER.warn('Could not update environment information due to: ', e)
        }
    }

    static void updateBuildInfo(String build) {
        try {
            jiraZephyrController.updateBuildInfo(cycle, build)
        } catch (Exception e) {
            LOGGER.warn('Could not update build information due to: ', e)
        }
    }

    static Cycle getCycle() {
        cycle
    }

    static void disableStepAttachments() {
        stepAttachmentsAllowed = false
    }

    static boolean stepAttachmentsAllowed() {
        stepAttachmentsAllowed
    }

}