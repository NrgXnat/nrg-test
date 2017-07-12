package org.nrg.testing.xnat;

import org.apache.commons.lang3.StringUtils;
import org.nrg.listeners.git.GitLogListener;
import org.nrg.listeners.jira.JIRATestNGListener;
import org.nrg.testing.BaseTestCase;
import org.nrg.testing.TestController;
import org.nrg.testing.annotations.JiraKey;
import org.nrg.testing.annotations.TestRequires;
import org.nrg.testing.annotations.XnatVersionLink;
import org.nrg.testing.enums.TestData;
import org.nrg.testing.file.FileIO;
import org.nrg.testing.jira.JIRAProperties;
import org.nrg.testing.jira.JIRASettings;
import org.nrg.testing.util.TestNgUtils;
import org.nrg.testing.xnat.conf.Settings;
import org.nrg.testing.xnat.conf.XNATProperties;
import org.nrg.testing.xnat.conf.XnatConfig;
import org.nrg.testing.xnat.rest.XnatRestDriver;
import org.nrg.testing.xnat.ssh.SSHConnection;
import org.nrg.testing.xnat.versions.XnatVersion;
import org.nrg.testing.xnat.versions.XnatVersionList;
import org.nrg.xnat.pojo.Project;
import org.nrg.xnat.pojo.Subject;
import org.nrg.xnat.pojo.User;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseXnatTest extends BaseTestCase {

    protected Project testSpecificProject;
    protected Subject testSpecificSubject;
    protected XnatRestDriver restDriver;
    protected List<User> userPool = new ArrayList<>();

    @BeforeSuite
    public void setupXnatTests(ITestContext testContext) {
        XnatVersionList.readXnatVersions();
        checkSettings();
        constructRestDriver();
        setupXnat();
        if (Settings.JIRA_SETTING) JIRATestNGListener.updateBuildInfo(restDriver.getBuildInfo());
        if (Settings.GITLOGS_SETTING) GitLogListener.init(Settings.MAIN_ADMIN_USERNAME, Settings.MAIN_ADMIN_PASS, Settings.BASEURL);
        handleSetupAnnotationRequirements();
    }

    /**
     * Handles annotated requirements at the class level and creates generic users required by class or tests
     */
    @BeforeClass
    public void handleClassRequirements() {
        int requiredUsers = 0;
        final Class<? extends BaseTestCase> testClass = this.getClass();

        final List<ITestNGMethod> classTests = getTestsByClass(testClass);

        if (classTests != null) {
            for (ITestNGMethod test : classTests) {
                final TestRequires requires = TestNgUtils.getAnnotation(test, TestRequires.class);
                if (requires != null && requires.user()) requiredUsers++;
            }
        }

        final TestRequires classRequires = testClass.getAnnotation(TestRequires.class);
        if (classRequires != null) {
            if (classRequires.user()) requiredUsers += numTestsToBeRunInClass(testClass);
            if (classRequires.db()) {
                TestNgUtils.assumeTrue(Settings.HAS_DB_INFO, "DB connection information is required for all tests in class: " + testClass.getSimpleName());
            }
            if (classRequires.dicomScp()) {
                TestNgUtils.assumeTrue(Settings.HAS_DICOM_RECEIVER_INFO, "DICOM SCP connection information is required for all tests in class: " + testClass.getSimpleName());
            }
            if (classRequires.ssh()) {
                TestNgUtils.assumeTrue(Settings.SSH_FUNCTIONS, "SSH connection information is required for all tests in class: " + testClass.getSimpleName());
            }
            if (classRequires.admin()) {
                TestNgUtils.assumeTrue(Settings.ADMIN_AVAILABLE, "XNAT admin account is required for all tests in class: " + testClass.getSimpleName());
            }
        }

        if (requiredUsers > 0) {
            userPool.clear();
            userPool.addAll(createGenericUsers(requiredUsers));
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void setupXnatTest(Method m, ITestContext testContext) {
        initializeTestRandomVariables();
        checkTestRequirements(m);
    }

    private void checkSettings() {
        Settings.checkNull(Settings.MAIN_USERNAME, XNATProperties.MAIN_USER);
        Settings.checkNull(Settings.MAIN_PASS, XNATProperties.MAIN_PASS);
        if (Settings.ADMIN_SETTING) {
            Settings.checkDependentNull(XNATProperties.ADMIN_SETTING, Settings.MAIN_ADMIN_USERNAME, XNATProperties.MAIN_ADMIN_USER);
            Settings.checkDependentNull(XNATProperties.ADMIN_SETTING, Settings.MAIN_ADMIN_PASS, XNATProperties.MAIN_ADMIN_PASS);
            Settings.checkDependentNull(XNATProperties.ADMIN_SETTING, Settings.ADMIN_USERNAME, XNATProperties.ADMIN_USER);
            Settings.checkDependentNull(XNATProperties.ADMIN_SETTING, Settings.ADMIN_PASS, XNATProperties.ADMIN_PASS);
        }
        Settings.checkNull(Settings.BASEURL, XNATProperties.BASEURL);
        if (Settings.INIT_SETTING) {
            Settings.checkDependentBooleans(XNATProperties.INIT_SETTING, XNATProperties.ADMIN_SETTING, Settings.ADMIN_SETTING);
        }
        if (Settings.NOTIFICATION_EMAILS != null) {
            Settings.checkDependentNull("Custom email notification", Settings.EMAIL, XNATProperties.EMAIL);
            Settings.checkDependentNull("Custom email notification", Settings.EMAIL_PASS, XNATProperties.EMAIL_PASS);
        }
        if (Settings.DYNAMIC_ORDERING) {
            if (Settings.QUEUE_SLOTS == 0) throw new RuntimeException(String.format("Dynamic ordering is being used, so %s must be set to a positive integer number of slots.", XNATProperties.QUEUE_SLOTS));
        }
        if (Settings.JIRA_SETTING) {
            Settings.checkDependentNull(XNATProperties.JIRA_SETTING, JIRASettings.JIRA_USER, JIRAProperties.JIRA_USER);
            Settings.checkDependentNull(XNATProperties.JIRA_SETTING, JIRASettings.JIRA_PASS, JIRAProperties.JIRA_PASS);
            Settings.checkDependentNull(XNATProperties.JIRA_SETTING, JIRASettings.JIRA_URL, JIRAProperties.JIRA_URL);
            Settings.checkDependentNull(XNATProperties.JIRA_SETTING, JIRASettings.PROJECT, JIRAProperties.JIRA_PROJECT);
            Settings.checkDependentNull(XNATProperties.JIRA_SETTING, JIRASettings.VERSION, JIRAProperties.JIRA_PROJECT_VERSION);
        }
        if (Settings.PRODUCE_PDF) {
            Settings.checkDependentBooleans(XNATProperties.PRODUCE_PDF, XNATProperties.JIRA_SETTING, Settings.JIRA_SETTING);
        }
    }

    protected void initializeTestRandomVariables() {
        testSpecificProject = new Project();
        testSpecificSubject = new Subject();
    }

    protected void checkTestRequirements(Method method) {
        final TestRequires testRequires = method.getAnnotation(TestRequires.class);
        if (testRequires != null) {
            if (testRequires.db()) {
                TestNgUtils.assumeTrue(Settings.HAS_DB_INFO, "DB connection information is required for test: " + method.getName());
            }
            if (testRequires.dicomScp()) {
                TestNgUtils.assumeTrue(Settings.HAS_DICOM_RECEIVER_INFO, "DICOM SCP connection information is required for test: " + method.getName());
            }
            if (testRequires.ssh()) {
                TestNgUtils.assumeTrue(Settings.SSH_FUNCTIONS, "SSH connection information is required for test: " + method.getName());
            }
            if (testRequires.admin()) {
                TestNgUtils.assumeTrue(Settings.ADMIN_AVAILABLE, "XNAT admin account is required for test: " + method.getName());
            }
        }
    }

    @Override
    protected String readJiraNumber(ITestNGMethod test) {
        final JiraKey key = TestNgUtils.getAnnotation(test, JiraKey.class);
        if (key != null) {
            final String simpleId = key.simpleKey();
            final XnatVersionLink[] versionLinks = key.versionMap();

            if (containsCurrentXnatVersion(versionLinks)) {
                return getJiraId(versionLinks);
            } else if (StringUtils.isNotEmpty(simpleId)) {
                return simpleId;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private String getJiraId(XnatVersionLink[] links) {
        for (XnatVersionLink link : links) {
            for (Class<? extends XnatVersion> xnatVersion : link.xnatVersions()) {
                if (XnatVersionList.getKey(xnatVersion).equals(Settings.XNAT_VERSION)) {
                    link.mappedValue();
                }
            }
        }
        return null;
    }

    private boolean containsCurrentXnatVersion(XnatVersionLink[] links) {
        for (XnatVersionLink link : links) {
            for (Class<? extends XnatVersion> xnatVersion : link.xnatVersions()) {
                if (XnatVersionList.getKey(xnatVersion).equals(Settings.XNAT_VERSION)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Takes care of all requirements that may be needed for individual tests/classes but that can be satisfied on startup
     */
    private void handleSetupAnnotationRequirements() {
        final List<String> alreadyDownloadedData = new ArrayList<>();
        boolean scriptsPushed = false;
        for (ITestNGMethod test : allRunningTests.keySet()) {
            final TestRequires testReq = TestNgUtils.getAnnotation(test, TestRequires.class);
            final Class<?> classObj = test.getRealClass();
            final TestRequires classReq = classObj.getAnnotation(TestRequires.class);
            getDataForAnnotation(alreadyDownloadedData, testReq);
            getDataForAnnotation(alreadyDownloadedData, classReq); // Annotation can be at the class level too
            if (!scriptsPushed) {
                if ((testReq != null && testReq.ssh()) || (classReq != null && classReq.ssh())) {
                    SSHConnection.pushScripts();
                    scriptsPushed = true;
                }
            }
        }
    }

    private void getDataForAnnotation(List<String> alreadyDownloadedData, TestRequires dataAnnotation) {
        if (dataAnnotation != null) {
            for (TestData dataElement : dataAnnotation.data()) {
                final String data = dataElement.getZipName();
                if (data != null && !alreadyDownloadedData.contains(data)) {
                    FileIO.getTestData(data);
                    FileIO.unzip(Settings.DATA_LOCATION, data);
                    alreadyDownloadedData.add(data);
                }
            }
        }
    }

    protected abstract void setupXnat();

    protected abstract List<User> createGenericUsers(int numUsers);

    protected User getGenericUser() {
        if (userPool.size() > 0) {
            final User user = userPool.get(0);
            userPool.remove(0);
            return user;
        }
        return null;
    }

    protected void constructRestDriver(XnatConfig xnatConfig) {
        restDriver = XnatRestDriver.getInstance(xnatConfig);
        if (testController == null) testController = new TestController();
        restDriver.setTestController(testController);
    }

    protected void constructRestDriver() {
        constructRestDriver(null);
    }

    public XnatRestDriver getRestDriver() {
        return restDriver;
    }

}
