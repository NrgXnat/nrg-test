package org.nrg.testing.xnat.conf

import com.jayway.restassured.specification.RequestSpecification
import org.apache.log4j.Logger
import org.nrg.testing.CommonStringUtils
import org.nrg.testing.FileIOUtils
import org.nrg.testing.TimeUtils
import org.nrg.testing.enums.TestBehavior
import org.nrg.testing.file.FileLocation
import org.nrg.testing.util.RandomHelper
import org.nrg.testing.xnat.ssh.SSHConnection
import org.nrg.xnat.versions.XnatVersion
import org.nrg.xnat.pogo.dicom.DicomScpReceiver
import org.nrg.xnat.rest.Credentials

import java.nio.file.Path
import java.nio.file.Paths

@SuppressWarnings('unused')
class Settings {

    private static final XNATProperties properties = new XNATProperties()
    private static final Logger LOGGER = Logger.getLogger(Settings)

    // values constant even with multiple XNATs in play
    public static final String EMAIL = properties.mainEmail // must be ahead of user initialization
    public static final String EMAIL_PASS = properties.mainEmailPassword
    public static final String DATA_LOCATION = FileLocation.getDataLocation()
    public static final String TARGET_LOCATION = FileLocation.getResultLocation()
    public static final String TIMELOG_LOCATION = FileLocation.getTimeLogsLocation()
    public static final String TEMP = properties.tempFolder
    public static final String DIRECTORY_NAME = "nrg_test_downloads_${TimeUtils.getTimestamp('uuuu-MM-dd_HH-mm-ss')}"
    public static final String TEMP_SUBDIR = generateTempSubdir()
    public static final String FAILED_SCREENSHOT_PATH = [TARGET_LOCATION, 'failed_test_screenshots'].join(File.separator)
    public static final String SCREENSHOT_PATH = [TARGET_LOCATION, 'test_step_screenshots'].join(File.separator)
    public static final String JENKINS_BUILD_URL = System.getenv('BUILD_URL')
    public static final boolean PRODUCE_PDF = properties.pdfSetting
    public static final boolean DOM_SETTING = properties.domSetting
    public static final boolean SETUP_MR_SCAN = properties.mrScanSetupSetting
    public static final TestBehavior BEHAVIOR_FOR_EXPECTED_FAILURES = properties.expectedFailureTestBehavior
    public static final boolean SKIP_EXPECTED_FAILURE = BEHAVIOR_FOR_EXPECTED_FAILURES == TestBehavior.SKIP
    public static final TestBehavior BEHAVIOR_FOR_MISSING_PLUGIN = properties.missingPluginTestBehavior
    public static final int DEFAULT_TIMEOUT = properties.defaultTimeout
    public static final String BROWSER = properties.browser
    public static final boolean JIRA_SETTING = properties.jiraSetting
    public static final boolean DYNAMIC_ORDERING = properties.dynamicOrderingSetting
    public static final int QUEUE_SLOTS = properties.queueSlots
    public static final String[] NOTIFICATION_EMAILS = properties.notificationEmails
    public static final boolean NOTIFICATION_SETTING = properties.notificationSetting
    public static final String NOTIFICATION_TITLE = properties.notificationTitle
    public static final boolean CHECK_DEPENDENCIES = properties.dependencyCheck
    public static final boolean TIMELOG_SETTING = properties.timelogSetting
    public static final boolean GITLOGS_SETTING = properties.gitlogSetting
    public static final boolean BASIC_MODE = properties.basicSetting
    public static final boolean LOGGING_ALLOWED = properties.loggingAllowedSetting
    public static final String FIREFOX_PATH = properties.firefoxPath
    public static final String SMTP_HOST = properties.smtpHost
    public static final int SMTP_PORT = properties.smtpPort
    public static final Properties SMTP_PROPERTIES = composeSmtpProperties()
    public static final boolean CS_SWARM_CAN_ENABLE = properties.csSwarmCanEnable
    public static final int CS_SWARM_TIMEOUT = properties.csSwarmTimeout

    // values that get fuzzy when multiple XNATs in play
    public static final String MAIN_USERNAME = properties.mainUser
    public static final String MAIN_PASS = properties.mainPassword
    public static final String MAIN_ADMIN_USERNAME = properties.mainAdminUser
    public static final String MAIN_ADMIN_PASS = properties.mainAdminPassword
    public static final String ADMIN_USERNAME = properties.adminUser
    public static final String ADMIN_PASS = properties.adminPassword
    public static final boolean ADMIN_AVAILABLE = ADMIN_USERNAME != null && ADMIN_PASS != null
    public static final Class<? extends XnatVersion> XNAT_VERSION = properties.XNATVersion
    public static final String BASEURL = CommonStringUtils.formatUrl(properties.baseURL)
    public static final XnatConfig DEFAULT_XNAT_CONFIG = XnatConfig.buildDefaultConfig()
    public static final List<XnatConfig> OTHER_XNAT_CONFIGS = properties.otherXnatConfigs
    public static final String HOSTURL = DEFAULT_XNAT_CONFIG.hostName
    public static final boolean INIT_SETTING = properties.initSetting
    public static final String DICOM_HOST = properties.dicomHost
    public static final int DICOM_PORT = properties.dicomPort
    public static final String DICOM_AETITLE = properties.dicomAetitle
    public static final DicomScpReceiver DEFAULT_RECEIVER = new DicomScpReceiver().aeTitle(DICOM_AETITLE).port(DICOM_PORT).enabled(true).host(DICOM_HOST)
    public static final boolean HAS_DICOM_RECEIVER_INFO = (DICOM_HOST != null) && (DICOM_PORT > 0) && (DICOM_AETITLE != null)
    public static final boolean ADMIN_SETTING = properties.adminSetting
    public static final String DB_URL = properties.databaseUrl
    public static final String DB_USER = properties.databaseUser
    public static final String DB_PASS = properties.databasePass
    public static final boolean HAS_DB_INFO = (DB_URL != null) && (DB_USER != null) && (DB_PASS != null)
    public static final String SSH_USER = properties.sshUser
    public static final String SSH_KEY_NAME = properties.sshPrivateKeyName
    public static final File SSH_KEY = getSshKey()
    public static final boolean HEADLESS = properties.headlessSetting
    public static final String TOMCAT_VERSION = properties.tomcatVersion
    public static final boolean SSH_FUNCTIONS = new SSHConnection().testSSH() // needs to come after TEMP_SUBDIR

    private static File getSshKey() {
        Paths.get(System.getProperty('user.home'), '.ssh', SSH_KEY_NAME).toFile()
    }

    private static Properties composeSmtpProperties() {
        final Properties props = new Properties()
        props.put('mail.smtp.host', SMTP_HOST)
        props.put('mail.smtp.port', SMTP_PORT)
        props
    }

    static RequestSpecification mainCredentials() {
        Credentials.build(DEFAULT_XNAT_CONFIG.mainUser)
    }

    static RequestSpecification mainAdminCredentials() {
        Credentials.build(DEFAULT_XNAT_CONFIG.mainAdminUser)
    }

    static RequestSpecification adminCredentials() {
        Credentials.build(DEFAULT_XNAT_CONFIG.adminUser)
    }

    static String generateTempSubdir() {
        final Path dirPath = Paths.get(TEMP, DIRECTORY_NAME)
        FileIOUtils.mkdirs(dirPath)
        dirPath.toFile().deleteOnExit()
        dirPath.toString()
    }

    static String getFailedScreenshotPath(String testClass) {
        [FAILED_SCREENSHOT_PATH, testClass].join(File.separator)
    }

    static String getScreenshotPath(String testClass) {
        [SCREENSHOT_PATH, testClass].join(File.separator)
    }

    static String getFailureScreenshotName(String testName) {
        "${testName}_failureScreenshot.png"
    }

    static String permuteSeleniumEmail() {
        final String emailId = EMAIL.substring(0, EMAIL.indexOf('@'))
        final String emailProvider = EMAIL.substring(EMAIL.indexOf('@'))

        if (emailProvider != '@gmail.com') {
            LOGGER.warn('Provided email address should be a gmail address so that tests can access the email address [unless the tests have access to PHI].')
        }

        "${emailId}+${RandomHelper.randomLetters(12)}${emailProvider}"
    }

}
