package org.nrg.testing.listeners.adapters.jira

import org.apache.log4j.Logger
import org.nrg.jira.components.zephyr.TestStatus

class NonInteractiveTest extends JIRATest {

    private static final Logger LOGGER = Logger.getLogger(NonInteractiveTest.class)

    NonInteractiveTest(String testName, String methodClass) {
        super(null, null, testName, null, methodClass)
    }

    @Override
    String getExecutionUrl() {
        null
    }

    @Override
    boolean hasSteps() {
        false
    }

    @Override
    String getJiraNumber() {
        null
    }

    @Override
    void createExecution() {}

    @Override
    void updateExecutionStatus(TestStatus status) {}

    @Override
    void postExecutionComment(String comment) {
        if (comment != null) LOGGER.info("Test ${testName} requested the following comment to be posted to JIRA: ${comment}")
    }

    @Override
    void postDefect(String issue) {
        if (issue != null) LOGGER.info("Test ${testName} attempted to link to defect in JIRA: ${issue}")
    }

    @Override
    void postExecutionAttachment(File attachment) {}

    @Override
    void postStepAttachment(File attachment, int step) {}

    @Override
    void postStepAttachment(File attachment) {}

    @Override
    void updateStepResult(int step, TestStatus status, String comment) {
        postExecutionComment(comment)
    }

    @Override
    void failTest() {}

    @Override
    void passTest() {}

    @Override
    void skipTest() {}

}
