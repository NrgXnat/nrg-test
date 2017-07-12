/*
 * org.nrg.selenium.email.SummaryEmail
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.testing.email;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.nrg.listeners.jira.JIRATest;
import org.nrg.listeners.jira.JIRATestNGListener;
import org.nrg.listeners.jira.failure.FailureCause;
import org.nrg.testing.CommonUtils;
import org.nrg.testing.jira.JIRASettings;
import org.nrg.testing.util.TestNgUtils;
import org.nrg.testing.xnat.conf.Settings;
import org.nrg.testing.xnat.rest.XnatRestDriver;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.*;

public class SummaryEmail {

    private static final Logger LOGGER = Logger.getLogger(SummaryEmail.class);

    public static void sendSummaryEmail(XnatRestDriver primaryXnatDriver, List<ITestResult> passedTests, Map<ITestNGMethod, FailureCause> failedTests, List<ITestResult> skippedTests) {
        final boolean allPassed = failedTests.size() + skippedTests.size() == 0;
        if (!Settings.NOTIFICATION_SETTING && allPassed) return; // if we don't want to send success emails AND all tests passed, return
        if (Settings.NOTIFICATION_EMAILS == null || Settings.EMAIL == null || Settings.NOTIFICATION_TITLE == null) return;
        // If we don't have anywhere to send it, anywhere to send it from, or anything to call it, return

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        Session session;

        try {
            Authenticator credentials = new Authenticator() {
                public PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(Settings.EMAIL, Settings.EMAIL_PASS);
                }
            };
            session = Session.getInstance(properties, credentials);
        } catch (Exception mex) {
            LOGGER.warn("Failed in connecting to gmail", mex);
            return;
        }

        Message message = new MimeMessage(session);

        try {
            message.setFrom(new InternetAddress(Settings.EMAIL));
            message.setRecipients(Message.RecipientType.TO, convertAddresses());

            String failureString;
            if (allPassed) {
                failureString = "All passing!";
            } else {
                failureString = String.format("%d failed and %d blocked tests", failedTests.size(), skippedTests.size());
            }

            message.setSubject(String.format("Results for %s: %s (%s)", Settings.NOTIFICATION_TITLE, failureString, CommonUtils.getTimestamp("yyyy-MM-dd")));

            message.setSentDate(new Date());
            final int totalNumTests = passedTests.size() + failedTests.size() + skippedTests.size();
            String messageContents = "Hello friend,<br /><br />";
            messageContents += String.format("A series of automated tests has just finished running on the test system located at %s. ", getLink(Settings.BASEURL, Settings.BASEURL));
            messageContents += String.format("A total number of %d tests ran, resulting in %d successful tests, " +
                    "%d failed tests, and %d skipped tests (blocked by failing tests or configuration). ", totalNumTests, passedTests.size(), failedTests.size(), skippedTests.size());
            if (JIRATestNGListener.getCycle() != null) messageContents += String.format("Test executions with attached screenshots and stack traces for failing tests may be found on the " +
                    "Test Cycle page in the NRG JIRA under the cycle: <a href=\"%s\">%s</a>. ", JIRATestNGListener.getCycle().getCycleUrl(JIRASettings.JIRA_URL, JIRASettings.PROJECT), JIRATestNGListener.getCycle().getName());
            if (Settings.JENKINS_BUILD_URL != null) messageContents += String.format("Authorized users can view test execution information on: %s. ", getLink(Settings.JENKINS_BUILD_URL, "jenkins"));
            messageContents += "<br /><br />A summary of the test results may be found below:<br />";
            messageContents += "Failing tests:<br />&emsp;&emsp;" + StringUtils.join(extractTests(primaryXnatDriver, failedTests), "<br />&emsp;&emsp;") + "<br />";
            messageContents += "Skipped tests:<br />&emsp;&emsp;" + StringUtils.join(extractTests(skippedTests), "<br />&emsp;&emsp;") + "<br />";
            messageContents += "Passing tests:<br />&emsp;&emsp;" + StringUtils.join(extractTests(passedTests), "<br />&emsp;&emsp;") + "<br />";
            messageContents += "<br /><br />Thank you,<br />NRG QA Team<br /><br /><br />If you're unsure why you're receiving this email, please contact <a href=\"mailto:moore.c@wustl.edu\">Charlie Moore</a> for assistance.";
            message.setContent(messageContents, "text/html");
            Transport.send(message);
            LOGGER.info("Summary email sent without exception.");
        } catch (MessagingException mex) {
            LOGGER.warn("Constructing and sending email failed", mex);
        }
    }

    private static InternetAddress[] convertAddresses() throws AddressException {
        String[] recipients = Settings.NOTIFICATION_EMAILS;
        InternetAddress[] addresses = new InternetAddress[recipients.length];
        for (int i = 0; i < recipients.length; i++) {
            addresses[i] = new InternetAddress(recipients[i]);
        }
        return addresses;
    }

    private static List<String> extractTests(List<ITestResult> tests) {
        if (tests.isEmpty()) return Collections.singletonList("");
        final List<String> testNames = new ArrayList<>();
        for (ITestResult testResult : tests) {
            testNames.add(appendJiraInfoToTest(testResult.getMethod()));
        }
        return testNames;
    }

    private static List<String> extractTests(XnatRestDriver restDriver, Map<ITestNGMethod, FailureCause> tests) { // Allow Map of TestName -> Failure/Skip reason
        if (tests.isEmpty()) return Collections.singletonList("");
        final List<String> testNames = new ArrayList<>();
        for (Map.Entry<ITestNGMethod, FailureCause> test : tests.entrySet()) {
            String baseTestString = appendJiraInfoToTest(test.getKey());
            if (test.getValue() != null) {
                baseTestString += " - " + test.getValue().getHTMLReason();
            }
            if (Settings.GITLOGS_SETTING && restDriver != null) {
                baseTestString += String.format(" (%s)", getLink(restDriver.formatXapiUrl("testlog", "log", TestNgUtils.getTestName(test.getKey())), "Download logs"));
            }
            testNames.add(baseTestString);
        }
        return testNames;
    }

    private static String appendJiraInfoToTest(ITestNGMethod testMethod) {
        final JIRATest jiraTest = JIRATestNGListener.getCurrentTest(testMethod);
        final String testName = TestNgUtils.getTestName(testMethod);
        return (jiraTest.getExecutionUrl() == null) ? testName : getLink(jiraTest.getExecutionUrl(), testName);
    }

    private static String getLink(String url, String linkText) {
        return String.format("<a href=\"%s\">%s</a>", url, linkText);
    }
}
