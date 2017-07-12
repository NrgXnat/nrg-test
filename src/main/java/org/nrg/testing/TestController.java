package org.nrg.testing;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.time.StopWatch;
import org.nrg.listeners.jira.JIRATest;
import org.nrg.listeners.jira.JIRATestNGListener;
import org.testng.ITestNGMethod;

import java.io.File;

import static org.testng.AssertJUnit.fail;

public class TestController {

    private String testName;
    private String testClass;
    private JIRATest currentTest;
    private StopWatch currentTestTimer;
    private MutableInt stepCounter;
    private boolean testRunning = false;

    public TestController() {
        stepCounter = new MutableInt(1);
    }

    public void startTestTimer() {
        currentTestTimer = CommonUtils.launchStopWatch();
    }

    public void selectJiraTest(ITestNGMethod test) {
        currentTest = JIRATestNGListener.getCurrentTest(test);
    }

    public void failTest(MutableInt step, String message) {
        currentTest.updateStepResult(step.getValue(), null, message);
        fail(message);
    }

    public void failBecause(String reason) {
        currentTest.setFailureReason(reason);
        failTest(stepCounter, reason);
    }

    public void failBecause(String reason, String jiraBug) {
        currentTest.setFailureReason(reason, jiraBug);
        failTest(stepCounter, reason);
    }

    public void postStepAttachment(File attachment, Integer step) {
        currentTest.postStepAttachment(attachment, step);
    }

    public void commentStep(String comment) {
        currentTest.updateStepResult(stepCounter.getValue(), null, comment);
    }

    public JIRATest getCurrentTest() {
        return currentTest;
    }

    public MutableInt getStepCounter() {
        return stepCounter;
    }

    public int getStep() {
        return (stepCounter == null) ? -1 : stepCounter.getValue();
    }

    public StopWatch getCurrentTestTimer() {
        return currentTestTimer;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public void setTestClass(String testClass) {
        this.testClass = testClass;
    }

    public String getTestName() {
        return testName;
    }

    public String getTestClass() {
        return testClass;
    }

    public boolean isTestRunning() {
        return testRunning;
    }

    public void setTestRunning(boolean testRunning) {
        this.testRunning = testRunning;
    }

}

