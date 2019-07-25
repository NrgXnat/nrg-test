package org.nrg.testing

import org.apache.commons.lang3.mutable.MutableInt
import org.apache.commons.lang3.time.StopWatch
import org.nrg.listeners.jira.JIRATest
import org.testng.ITestNGMethod

import static org.testng.AssertJUnit.fail

class TestController {

    String testName
    String testClass
    boolean testRunning = false
    private JIRATest currentTest
    private StopWatch currentTestTimer
    private MutableInt stepCounter

    TestController() {
        stepCounter = new MutableInt(1)
    }

    void startTestTimer() {
        currentTestTimer = CommonUtils.launchStopWatch()
    }

    void selectJiraTest(ITestNGMethod test) {
        currentTest = JIRATestNGListener.getCurrentTest(test)
    }

    void failTest(MutableInt step, String message) {
        currentTest.updateStepResult(step.getValue(), null, message)
        fail(message)
    }

    void failBecause(String reason) {
        currentTest.setFailureReason(reason)
        failTest(stepCounter, reason)
    }

    void failBecause(String reason, String jiraBug) {
        currentTest.setFailureReason(reason, jiraBug)
        failTest(stepCounter, reason)
    }

    void postStepAttachment(File attachment, Integer step) {
        currentTest.postStepAttachment(attachment, step)
    }

    void commentStep(String comment) {
        currentTest.updateStepResult(stepCounter.getValue(), null, comment)
    }

    JIRATest getCurrentTest() {
        return currentTest
    }

    MutableInt getStepCounter() {
        return stepCounter
    }

    int getStep() {
        return (stepCounter == null) ? -1 : stepCounter.getValue()
    }

    StopWatch getCurrentTestTimer() {
        return currentTestTimer
    }

}

