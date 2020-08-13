package org.nrg.testing.email

import org.apache.log4j.Logger
import org.nrg.jira.components.zephyr.Cycle
import org.nrg.testing.CommonStringUtils
import org.nrg.testing.TestNgUtils
import org.nrg.testing.TimeUtils
import org.nrg.testing.annotations.TestedApiSpec
import org.nrg.testing.annotations.TestedApiSpecs
import org.nrg.testing.jira.JIRASettings
import org.nrg.testing.listeners.adapters.jira.JIRATest
import org.nrg.testing.listeners.adapters.jira.JIRATestListener
import org.nrg.testing.listeners.adapters.jira.failure.FailureCause
import org.nrg.testing.xnat.conf.Settings
import org.testng.ITestNGMethod
import org.testng.ITestResult

import javax.mail.*
import javax.mail.internet.AddressException
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class SummaryEmail {

    private static final Logger LOGGER = Logger.getLogger(SummaryEmail)
    private final boolean notificationSetting = Settings.NOTIFICATION_SETTING
    private final String[] notificationEmails = Settings.NOTIFICATION_EMAILS
    private final String emailAddress = Settings.EMAIL
    private final String notificationTitle = Settings.NOTIFICATION_TITLE
    private final String jenkinsUrl = Settings.JENKINS_BUILD_URL
    private final boolean gitlogSetting = Settings.GITLOGS_SETTING
    private final String baseUrl = Settings.BASEURL
    private final Cycle cycle = (Settings.JIRA_SETTING) ? JIRATestListener.getCycle() : null
    private final String jiraUrl = (Settings.JIRA_SETTING) ? JIRASettings.JIRA_URL : null
    private final String jiraProject = (Settings.JIRA_SETTING) ? JIRASettings.PROJECT : null
    private final List<ITestResult> passedTests
    private final Map<ITestNGMethod, FailureCause> failedTests
    private final Map<ITestNGMethod, FailureCause> skippedTests

    SummaryEmail(List<ITestResult> passedTests, Map<ITestNGMethod, FailureCause> failedTests, Map<ITestNGMethod, FailureCause> skippedTests) {
        this.passedTests = passedTests
        this.failedTests = failedTests
        this.skippedTests = skippedTests
    }

    void sendSummaryEmail() {
        final boolean allPassed = failedTests.isEmpty() && skippedTests.isEmpty()
        if (!notificationSetting && allPassed) return // if we don't want to send success emails AND all tests passed, return
        if (notificationEmails == null || emailAddress == null || notificationTitle == null) return
        // If we don't have anywhere to send it, anywhere to send it from, or anything to call it, return

        final Message message = new MimeMessage(Session.getInstance(Settings.SMTP_PROPERTIES))

        try {
            message.setFrom(new InternetAddress(emailAddress))
            message.setRecipients(Message.RecipientType.TO, convertAddresses())

            final String failureString = allPassed ? 'All passing!' : "${failedTests.size()} failed and ${skippedTests.size()} blocked tests"
            message.setSubject("Results for ${notificationTitle}: ${failureString} (${TimeUtils.getTimestamp('uuuu-MM-dd')})")
            message.setSentDate(new Date())
            final int totalNumTests = passedTests.size() + failedTests.size() + skippedTests.size()

            final String messageContents = new MessageTemplate('summary_email.html').
                    replace('%num_total%', totalNumTests).
                    replace('%num_pass%', passedTests.size()).
                    replace('%num_fail%', failedTests.size()).
                    replace('%num_skip%', skippedTests.size()).
                    replace('%jira%', (cycle != null) ? "Test executions may be found on the Test Cycle page in the NRG JIRA under the cycle: ${getLink(cycle.getCycleUrl(jiraUrl, jiraProject), cycle.name)}. " : '').
                    replace('%jenkins%', (jenkinsUrl != null) ? "Authorized users can view test execution information on: ${getLink(jenkinsUrl, 'jenkins')}. " : '').
                    replace('%fail_list%', joinTests(extractTests(failedTests))).
                    replace('%skip_list%', joinTests(extractTests(skippedTests))).
                    replace('%pass_list%', joinTests(extractTests(passedTests))).
                    read()

            message.setContent(messageContents, 'text/html')
            Transport.send(message)
            LOGGER.info('Summary email sent without exception.')
        } catch (MessagingException mex) {
            LOGGER.warn('Constructing and sending email failed', mex)
        }
    }

    private InternetAddress[] convertAddresses() throws AddressException {
        notificationEmails.collect { email ->
            new InternetAddress(email)
        } as InternetAddress[]
    }

    private List<String> extractTests(List<ITestResult> tests) {
        if (tests.isEmpty()) {
            ['']
        } else {
            tests.collect { testResult ->
                formatTestString(testResult.method)
            }
        }
    }

    private List<String> extractTests( Map<ITestNGMethod, FailureCause> tests) { // Allow Map of TestName -> Failure/Skip reason
        if (tests.isEmpty()) {
            ['']
        } else {
            tests.collect { method, cause ->
                String testString = formatTestString(method)
                if (cause != null) {
                    testString += " - ${cause.getHTMLReason()}"
                }
                if (gitlogSetting) {
                    testString += " (${getLink(CommonStringUtils.formatUrl(baseUrl, '/xapi/testlog/log', TestNgUtils.getTestName(method)), 'Download logs')})"
                }
                testString
            }
        }
    }

    private String formatTestString(ITestNGMethod testMethod) {
        final JIRATest jiraTest = JIRATestListener.getJiraTest(testMethod)
        final String testName = TestNgUtils.getTestName(testMethod)
        final String testLink = (jiraTest.executionUrl == null) ? testName : getLink(jiraTest.executionUrl, testName)
        testLink + readApiSpecs(testMethod)
    }

    private String readApiSpecs(ITestNGMethod testMethod) {
        final TestedApiSpecs apiSpecs = TestNgUtils.getAnnotation(testMethod, TestedApiSpecs)
        final TestedApiSpec singleSpec = TestNgUtils.getAnnotation(testMethod, TestedApiSpec)
        if (apiSpecs != null) {
            final String specsString = apiSpecs.value().collect { spec ->
                readSingleSpec(spec)
            }.join(', ')
            " [${specsString}]"
        } else if (singleSpec != null) {
            " [${readSingleSpec(singleSpec)}]"
        } else {
            ''
        }
    }

    private String readSingleSpec(TestedApiSpec singleSpec) {
        "{${singleSpec.method().collect { it.name() }.join(', ')} to ${singleSpec.url().join(', ')}}"
    }

    private String joinTests(List<String> testStringList) {
        testStringList.join('<br />&emsp;&emsp;')
    }

    private String getLink(String url, String linkText) {
        "<a href=\'${url}\'>${linkText}</a>"
    }

}
