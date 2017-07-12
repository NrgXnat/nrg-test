package org.nrg.testing.xnat.conf;

import com.jayway.restassured.specification.RequestSpecification;
import org.nrg.testing.CommonUtils;
import org.nrg.testing.auth.Credentials;
import org.nrg.testing.file.FileIO;
import org.nrg.testing.file.FileLocation;
import org.nrg.testing.xnat.ssh.SSHConnection;
import org.nrg.xnat.pojo.dicom.DicomScpReceiver;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class Settings {

    private static final XNATProperties properties = new XNATProperties();

    // values constant even with multiple XNATs in play
    public static final String EMAIL = properties.getMainEmail(); // must be ahead of user initialization
    public static final String EMAIL_PASS = properties.getMainEmailPassword();
    public static final String DATA_LOCATION = FileLocation.fileLocation.getDataLocation();
    public static final String TARGET_LOCATION = FileLocation.fileLocation.getResultLocation();
    public static final String CONFIG_LOCATION = FileLocation.fileLocation.getConfigLocation();
    public static final String TIMELOG_LOCATION = FileLocation.fileLocation.getTimeLogsLocation();
    public static final String TEMP = properties.getTempFolder();
    public static final Calendar calendar = Calendar.getInstance();
    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    public static final String directoryName = "seleniumDownloads_" + sdf.format(calendar.getTime());
    public static final String TEMP_SUBDIR = generateTempSubdir();
    public static final String FAILED_SCREENSHOT_PATH = TARGET_LOCATION + File.separator + "failed_test_screenshots";
    public static final String SCREENSHOT_PATH = TARGET_LOCATION + File.separator + "test_step_screenshots";
    public static final String JENKINS_BUILD_URL = System.getenv("BUILD_URL");
    public static final boolean PRODUCE_PDF = properties.getPdfSetting();
    public static final boolean DOM_SETTING = properties.getDomSetting();
    public static final int DEFAULT_TIMEOUT = properties.getDefaultTimeout();
    public static final String BROWSER = properties.getBrowser();
    public static final boolean JIRA_SETTING = properties.getJiraSetting();
    public static final boolean DYNAMIC_ORDERING = properties.getDynamicOrderingSetting();
    public static final boolean GLPK_AVAILABLE = properties.getGLPKSetting();
    public static final int QUEUE_SLOTS = properties.getQueueSlots();
    public static final String[] NOTIFICATION_EMAILS = properties.getNotificationEmails();
    public static final boolean NOTIFICATION_SETTING = properties.getNotificationSetting();
    public static final String NOTIFICATION_TITLE = properties.getNotificationTitle();
    public static final boolean CHECK_DEPENDENCIES = properties.getDependencyCheck();
    public static final boolean TIMELOG_SETTING = properties.getTimelogSetting();
    public static final boolean GITLOGS_SETTING = properties.getGitlogSetting();
    public static final boolean BASIC_MODE = properties.getBasicSetting();

    // values that get fuzzy when multiple XNATs in play
    public static final String MAIN_USERNAME = properties.getMainUser();
    public static final String MAIN_PASS = properties.getMainPassword();
    public static final String MAIN_ADMIN_USERNAME = properties.getMainAdminUser();
    public static final String MAIN_ADMIN_PASS = properties.getMainAdminPassword();
    public static final String ADMIN_USERNAME = properties.getAdminUser();
    public static final String ADMIN_PASS = properties.getAdminPassword();
    public static final boolean ADMIN_AVAILABLE = ADMIN_USERNAME != null && ADMIN_PASS != null;
    public static final String XNAT_VERSION = properties.getXNATVersion();
    public static final String BASEURL = CommonUtils.formatUrl(properties.getBaseURL());
    public static final String HOSTURL = getHost();
    public static final XnatConfig DEFAULT_XNAT_CONFIG = XnatConfig.buildDefaultConfig();
    public static final List<XnatConfig> OTHER_XNAT_CONFIGS = properties.getOtherXnatConfigs();
    public static final boolean INIT_SETTING = properties.getInitSetting();
    public static final String DICOM_HOST = properties.getDicomHost();
    public static final int DICOM_PORT = properties.getDicomPort();
    public static final String DICOM_AETITLE = properties.getDicomAetitle();
    public static final DicomScpReceiver DEFAULT_RECEIVER = new DicomScpReceiver().aeTitle(DICOM_AETITLE).port(DICOM_PORT).enabled(true).host(DICOM_HOST);
    public static final boolean HAS_DICOM_RECEIVER_INFO = (DICOM_HOST != null) && (DICOM_PORT != -1) && (DICOM_AETITLE != null);
    public static final boolean ADMIN_SETTING = properties.getAdminSetting();
    public static final String DB_URL = properties.getDatabaseUrl();
    public static final String DB_USER = properties.getDatabaseUser();
    public static final String DB_PASS = properties.getDatabasePass();
    public static final boolean HAS_DB_INFO = (DB_URL != null) && (DB_USER != null) && (DB_PASS != null);
    public static final String SSH_USER = properties.getSshUser();
    public static final String SSH_KEY_NAME = properties.getSshPrivateKeyName();
    public static final File SSH_KEY = getSshKey();
    public static final String TOMCAT_VERSION = properties.getTomcatVersion();
    public static final boolean SSH_FUNCTIONS = SSHConnection.testSSH(); // needs to come after TEMP_SUBDIR

    private static String getHost() {
        try {
            return new URI(BASEURL).getHost();
        } catch (URISyntaxException use) {
            throw new RuntimeException("Couldn't get host from BASEURL", use);
        }
    }

    private static File getSshKey() {
        return Paths.get(System.getProperty("user.home"), ".ssh", SSH_KEY_NAME).toFile();
    }

    public static RequestSpecification mainCredentials() {
        return Credentials.build(DEFAULT_XNAT_CONFIG.getMainUser());
    }

    @SuppressWarnings("unused")
    public static RequestSpecification mainAdminCredentials() {
        return Credentials.build(DEFAULT_XNAT_CONFIG.getMainAdminUser());
    }

    public static RequestSpecification adminCredentials() {
        return Credentials.build(DEFAULT_XNAT_CONFIG.getAdminUser());
    }

    public static String generateTempSubdir() {
        final String dir = Paths.get(Settings.TEMP, directoryName).toString();
        FileIO.mkdirs(dir);
        return dir;
    }

    public static String getFailedScreenshotPath(String testClass) {
        return Settings.FAILED_SCREENSHOT_PATH + File.separator + testClass;
    }

    public static String getScreenshotPath(String testClass) {
        return Settings.SCREENSHOT_PATH + File.separator + testClass;
    }

    public static String getFailureScreenshotName(String testName) {
        return testName + "_failureScreenshot.png";
    }

    public static void checkNull(String setting, String property) {
        if (setting == null) throw new RuntimeException(String.format("Required value found to be null. Property must be set as %s.", property));
    }

    public static void checkDependentNull(String firstSetting, String subSetting, String subSettingProp) {
        if (subSetting == null) throw new RuntimeException(String.format("%s is set to true, which means %s must also be set up.", firstSetting, subSettingProp));
    }

    public static void checkDependentBooleans(String firstSetting, String subSetting, boolean subSettingProp) {
        if (!subSettingProp) throw new RuntimeException(String.format("%s is set to true, which means that %s must also be.", firstSetting, subSetting));
    }

}
