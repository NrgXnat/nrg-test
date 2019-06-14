package org.nrg.testing.email;

import com.jayway.restassured.internal.http.Method;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.nrg.listeners.jira.JIRATest;
import org.nrg.listeners.jira.JIRATestNGListener;
import org.nrg.listeners.jira.failure.FailureCause;
import org.nrg.testing.CommonUtils;
import org.nrg.testing.annotations.TestedApiSpecs;
import org.nrg.testing.jira.JIRASettings;
import org.nrg.testing.util.TestNgUtils;
import org.nrg.testing.xnat.conf.Settings;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.*;

public class SummaryEmail {

    private static final Logger LOGGER = Logger.getLogger(SummaryEmail.class);

    public static void sendSummaryEmail(List<ITestResult> passedTests, Map<ITestNGMethod, FailureCause> failedTests, List<ITestResult> skippedTests) {
        final boolean allPassed = failedTests.size() + skippedTests.size() == 0;
        if (!Settings.NOTIFICATION_SETTING && allPassed) return; // if we don't want to send success emails AND all tests passed, return
        if (Settings.NOTIFICATION_EMAILS == null || Settings.EMAIL == null || Settings.EMAIL_PASS == null || Settings.NOTIFICATION_TITLE == null) return;
        // If we don't have anywhere to send it, anywhere to send it from, or anything to call it, return

        final Properties properties = new Properties();
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

        final Message message = new MimeMessage(session);

        try {
            message.setFrom(new InternetAddress(Settings.EMAIL));
            message.setRecipients(Message.RecipientType.TO, convertAddresses());

            final String failureString = allPassed ? "All passing!" : String.format("%d failed and %d blocked tests", failedTests.size(), skippedTests.size());
            message.setSubject(String.format("Results for %s: %s (%s)", Settings.NOTIFICATION_TITLE, failureString, CommonUtils.getTimestamp("yyyy-MM-dd")));
            message.setSentDate(new Date());
            final int totalNumTests = passedTests.size() + failedTests.size() + skippedTests.size();

            final String messageContents = new EmailTemplate("summary_email.html").
                    replace("%num_total%", totalNumTests).
                    replace("%num_pass%", passedTests.size()).
                    replace("%num_fail%", failedTests.size()).
                    replace("%num_skip%", skippedTests.size()).
                    replace("%jira%", (JIRATestNGListener.getCycle() != null) ? String.format("Test executions may be found on the Test Cycle page in the NRG JIRA under the cycle: %s.", getLink(JIRATestNGListener.getCycle().getCycleUrl(JIRASettings.JIRA_URL, JIRASettings.PROJECT), JIRATestNGListener.getCycle().getName())) : "").
                    replace("%jenkins%", (Settings.JENKINS_BUILD_URL != null) ? String.format("Authorized users can view test execution information on: %s. ", getLink(Settings.JENKINS_BUILD_URL, "jenkins")) : "").
                    replace("%fail_list%", joinTests(extractTests(failedTests))).
                    replace("%skip_list%", joinTests(extractTests(skippedTests))).
                    replace("%pass_list%", joinTests(extractTests(passedTests))).
                    read();

            message.setContent(messageContents, "text/html");
            Transport.send(message);
            LOGGER.info("Summary email sent without exception.");
        } catch (MessagingException mex) {
            LOGGER.warn("Constructing and sending email failed", mex);
        }
    }

    private static InternetAddress[] convertAddresses() throws AddressException {
        final List<InternetAddress> addresses = new ArrayList<>();
        for (String recipient : Settings.NOTIFICATION_EMAILS) {
            addresses.add(new InternetAddress(recipient));
        }
        return addresses.toArray(new InternetAddress[]{});
    }

    private static List<String> extractTests(List<ITestResult> tests) {
        if (tests.isEmpty()) return Collections.singletonList("");
        final List<String> testNames = new ArrayList<>();
        for (ITestResult testResult : tests) {
            testNames.add(formatTestString(testResult.getMethod()));
        }
        return testNames;
    }

    private static List<String> extractTests( Map<ITestNGMethod, FailureCause> tests) { // Allow Map of TestName -> Failure/Skip reason
        if (tests.isEmpty()) return Collections.singletonList("");
        final List<String> testNames = new ArrayList<>();
        for (Map.Entry<ITestNGMethod, FailureCause> test : tests.entrySet()) {
            String baseTestString = formatTestString(test.getKey());
            if (test.getValue() != null) {
                baseTestString += " - " + test.getValue().getHTMLReason();
            }
            if (Settings.GITLOGS_SETTING) {
                baseTestString += String.format(" (%s)", getLink(CommonUtils.formatUrl(Settings.BASEURL, "/xapi/testlog/log", TestNgUtils.getTestName(test.getKey())), "Download logs"));
            }
            testNames.add(baseTestString);
        }
        return testNames;
    }

    private static String formatTestString(ITestNGMethod testMethod) {
        final JIRATest jiraTest = JIRATestNGListener.getCurrentTest(testMethod);
        final String testName = TestNgUtils.getTestName(testMethod);
        final String testLink = (jiraTest.getExecutionUrl() == null) ? testName : getLink(jiraTest.getExecutionUrl(), testName);
        return testLink + readApiSpecs(testMethod);
    }

    private static String readApiSpecs(ITestNGMethod testMethod) {
        final TestedApiSpecs apiSpecs = TestNgUtils.getAnnotation(testMethod, TestedApiSpecs.class);
        final TestedApiSpecs.Spec singleSpec = TestNgUtils.getAnnotation(testMethod, TestedApiSpecs.Spec.class);
        if (apiSpecs != null) {
            final List<String> readSpecs = new ArrayList<>();
            for (TestedApiSpecs.Spec spec : apiSpecs.value()) {
                readSpecs.add(readSingleSpec(spec));
            }
            return String.format(" [%s]", StringUtils.join(readSpecs, ", "));
        } else if (singleSpec != null) {
            return String.format(" [%s]", readSingleSpec(singleSpec));
        } else {
            return "";
        }
    }

    private static String readSingleSpec(TestedApiSpecs.Spec singleSpec) {
        final List<String> methods = new ArrayList<>();
        for (Method method : singleSpec.method()) {
            methods.add(method.name());
        }
        return String.format("{%s to %s}", StringUtils.join(methods, ", "), StringUtils.join(singleSpec.url(), ", "));
    }

    private static String joinTests(List<String> testStringList) {
        return StringUtils.join(testStringList, "<br />&emsp;&emsp;");
    }

    private static String getLink(String url, String linkText) {
        return String.format("<a href=\"%s\">%s</a>", url, linkText);
    }

}
