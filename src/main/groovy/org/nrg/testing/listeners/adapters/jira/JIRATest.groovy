package org.nrg.testing.listeners.adapters.jira

import org.apache.log4j.Logger
import org.nrg.jira.JiraZephyrController
import org.nrg.jira.components.zephyr.Cycle
import org.nrg.jira.components.zephyr.Execution
import org.nrg.jira.components.zephyr.TestStatus
import org.nrg.jira.components.zephyr.TestStep
import org.nrg.jira.exceptions.JiraZephyrException
import org.nrg.testing.listeners.adapters.jira.failure.FailureCause
import org.nrg.testing.listeners.adapters.jira.failure.FailureExplanation
import org.nrg.testing.listeners.adapters.jira.failure.FailureWithExistingBug

class JIRATest {

    private static final Logger LOGGER = Logger.getLogger(JIRATest.class)
    JiraZephyrController jiraZephyrController
    Cycle cycle
    Execution execution
    List<TestStep> testSteps = []
    String testName
    String jiraNumber
    String methodClass
    int lastMarkedStep = 0
    FailureCause failureReason
    FailureCause skipReason

    JIRATest(JiraZephyrController jiraZephyrController, Cycle cycle, String testName, String jiraNumber, String methodClass) {
        this.jiraZephyrController = jiraZephyrController
        this.cycle = cycle
        this.testName = testName
        this.jiraNumber = jiraNumber
        this.methodClass = methodClass
    }

    String getExecutionUrl() {
        final String query = URLEncoder.encode("cycleName=\"${cycle.name}\"", 'UTF-8')
        jiraZephyrController.formatJiraUrl('secure/enav/#', "${execution.id}?query=${query}")
    }

    boolean hasSteps() {
        !testSteps.isEmpty()
    }

    void setFailureReason(String failureReason) {
        this.failureReason = new FailureExplanation(failureReason)
    }

    void setFailureReason(String failureReason, String jiraBugNumber) {
        this.failureReason = new FailureWithExistingBug(failureReason, jiraBugNumber, jiraZephyrController)
        postDefect(jiraBugNumber)
    }

    void setSkipReason(String jiraBugNumber) {
        skipReason = new FailureWithExistingBug('Test skipped due to existing bug', jiraBugNumber, jiraZephyrController)
        postDefect(jiraBugNumber)
    }

    void createExecution() {
        try {
            execution = jiraZephyrController.createExecution(cycle, jiraNumber)
            testSteps = execution.testSteps
        } catch (Exception e) {
            LOGGER.warn("Failed to create execution for test: ${jiraNumber}", e)
        }
    }

    void updateExecutionStatus(TestStatus status) {
        try {
            jiraZephyrController.updateExecutionStatus(execution, status)
        } catch (Exception e) {
            LOGGER.warn('Error occurred when updating execution.', e)
        }
    }

    void postExecutionComment(String comment) {
        try {
            jiraZephyrController.postExecutionComment(execution, comment)
        } catch (Exception e) {
            LOGGER.warn('Error occurred when posting execution comment.', e)
        }
    }

    void postDefect(String defect) {
        try {
            jiraZephyrController.postDefect(execution, defect)
        } catch (Exception e) {
            LOGGER.warn('Error occurred when posting defect.', e)
        }
    }

    void postExecutionAttachment(File attachment) {
        try {
            jiraZephyrController.postExecutionAttachment(execution, attachment)
        } catch (Exception e) {
            LOGGER.warn('Unknown error when posting attachment to JIRA.', e)
        }
    }

    void postStepAttachment(File attachment, int step) {
        if (!JIRATestListener.stepAttachmentsAllowed()) {
            LOGGER.debug("Step attachments are not allowed. Skipping posting of ${attachment}")
        } else {
            try {
                jiraZephyrController.postStepAttachment(getNaturalStep(step), attachment)
            } catch (IndexOutOfBoundsException ignored) {
                logStepMissing(step)
            } catch (Exception e) {
                LOGGER.warn('Unknown error when posting step attachment to JIRA.', e)
            }
        }
    }

    void postStepAttachment(File attachment) {
        postStepAttachment(attachment, lastMarkedStep + 1)
    }

    /**
     * Updates the step in JIRA
     * @param step     Index for the step, numbered <i>naturally</i> (i.e. starting with 1)
     * @param status   Status for the step
     * @param comment  Comment for the step
     */
    void updateStepResult(int step, TestStatus status, String comment) {
        if (hasSteps()) {
            final TestStep executionStep
            try {
                executionStep = getNaturalStep(step) // natural ordering of test steps 1, 2, 3, ... to match what is returned by ZAPI
            } catch (IndexOutOfBoundsException ignored) {
                logStepMissing(step)
                return
            }
            try {
                jiraZephyrController.updateStepResult(executionStep, status, comment)
            } catch (JiraZephyrException e) {
                LOGGER.warn('Could not update JIRA execution step due to error.', e)
            }
            if (status != null) { // If we only want to add a comment, to fail the test afterwards we don't want to make it think this step is over
                lastMarkedStep = step
            }
        }
    }

    void failTest() {
        if (lastMarkedStep <= testSteps.size()) {
            updateStepResult(lastMarkedStep + 1, TestStatus.FAIL, null) // If we already marked the last step, there is no next step (think pipeline tests that fail the whole test after all steps are done)
        }
        updateExecutionStatus(TestStatus.FAIL)
    }

    void passTest() {
        updateExecutionStatus(TestStatus.PASS)
    }

    void skipTest() {
        updateExecutionStatus(TestStatus.BLOCKED)
    }

    private void logStepMissing(int step) {
        LOGGER.warn("Step ${step} not found in test: ${testName}")
    }

    private TestStep getNaturalStep(int step) throws IndexOutOfBoundsException {
        testSteps[step - 1] // natural ordering of test steps 1, 2, 3, ... to match what is returned by ZAPI
    }

}