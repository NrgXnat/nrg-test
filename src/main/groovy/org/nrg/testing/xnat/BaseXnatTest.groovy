package org.nrg.testing.xnat

import groovy.util.logging.Log4j
import org.apache.commons.lang3.StringUtils
import org.nrg.testing.BaseTestCase
import org.nrg.testing.TestController
import org.nrg.testing.TestNgUtils
import org.nrg.testing.annotations.ExpectedFailure
import org.nrg.testing.annotations.JiraKey
import org.nrg.testing.annotations.TestRequires
import org.nrg.testing.annotations.XnatVersionLink
import org.nrg.testing.email.EmailQuery
import org.nrg.testing.enums.PluginDependencyCheckState
import org.nrg.testing.enums.TestBehavior
import org.nrg.testing.enums.TestData
import org.nrg.testing.jira.JIRAProperties
import org.nrg.testing.jira.JIRASettings
import org.nrg.testing.listeners.adapters.git.GitLogListener
import org.nrg.testing.listeners.adapters.jira.JIRATestListener
import org.nrg.testing.util.Version
import org.nrg.testing.xnat.conf.Settings
import org.nrg.testing.xnat.conf.XNATProperties
import org.nrg.testing.xnat.conf.XnatConfig
import org.nrg.testing.xnat.plugins.PluginDependencyCheck
import org.nrg.testing.xnat.plugins.PluginDependencyManager
import org.nrg.testing.xnat.rest.XnatRestDriver
import org.nrg.xnat.interfaces.XnatInterface
import org.nrg.xnat.pogo.Project
import org.nrg.xnat.pogo.SiteConfig
import org.nrg.xnat.pogo.Subject
import org.nrg.xnat.pogo.XnatPlugin
import org.nrg.xnat.pogo.users.User
import org.nrg.xnat.subinterfaces.XnatFunctionalitySubinterface
import org.testng.ITestContext
import org.testng.ITestNGMethod
import org.testng.SkipException
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.BeforeMethod
import org.testng.annotations.BeforeSuite
import org.testng.annotations.DataProvider

import java.lang.reflect.Method

@Log4j
abstract class BaseXnatTest extends BaseTestCase {

    protected Project testSpecificProject
    protected Subject testSpecificSubject
    protected XnatRestDriver restDriver = initRestDriver()
    protected List<User> userPool = []

    protected final User mainUser = Settings.DEFAULT_XNAT_CONFIG.mainUser
    protected final User mainAdminUser = Settings.DEFAULT_XNAT_CONFIG.mainAdminUser
    private final List<XnatPlugin> installedPlugins = []
    private SiteConfig siteConfigRestoration
    public static final String CS_BACKENDS_DATA_PROVIDER = 'backend'

    @BeforeSuite(alwaysRun = true)
    void setupXnatTests(ITestContext testContext) {
        EmailQuery.setStartTime()
        validateSettings()
        constructRestDriver()
        if (Settings.INIT_SETTING) {
            setupXnat()
        }
        if (Settings.JIRA_SETTING) {
            JIRATestListener.updateBuildInfo(restDriver.buildInfo)
        }
        if (Settings.GITLOGS_SETTING) {
            GitLogListener.init(Settings.MAIN_ADMIN_USERNAME, Settings.MAIN_ADMIN_PASS, Settings.BASEURL)
        }
        handleSetupAnnotationRequirements()
    }

    /**
     * Handles annotated requirements at the class level and creates generic users required by class or tests
     */
    @BeforeClass(alwaysRun = true)
    void handleClassRequirements() {
        noteInitialConfigSettings()

        int requiredUsers = 0

        final Class<? extends BaseTestCase> testClass = this.class as Class<? extends BaseTestCase>
        final String testClassName = testClass.simpleName
        final TestRequires classRequires = testClass.getAnnotation(TestRequires)
        final ExpectedFailure classExpectedFailure = testClass.getAnnotation(ExpectedFailure)

        (getTestsByClass(testClass) ?: []).each { test ->
            final TestRequires requires = TestNgUtils.getAnnotation(test, TestRequires)
            if (requires != null) {
                requiredUsers += requires.users()
            }
            if (Settings.SKIP_EXPECTED_FAILURE && classExpectedFailure != null) {
                JIRATestListener.getJiraTest(test).setSkipReason(classExpectedFailure.jiraIssue())
            }
        }

        if (Settings.SKIP_EXPECTED_FAILURE && classExpectedFailure != null) {
            throw new SkipException("Tests all skipped due to expected failure in class: ${testClassName}")
        }

        if (classRequires != null) {
            requiredUsers += classRequires.users()
            if (classRequires.db()) {
                TestNgUtils.assumeTrue(Settings.HAS_DB_INFO, "DB connection information is required for all tests in class: ${testClassName}")
            }
            if (classRequires.dicomScp()) {
                TestNgUtils.assumeTrue(Settings.HAS_DICOM_RECEIVER_INFO, "DICOM SCP connection information is required for all tests in class: ${testClassName}")
            }
            if (classRequires.ssh()) {
                TestNgUtils.assumeTrue(Settings.SSH_FUNCTIONS, "SSH connection information is required for all tests in class: ${testClassName}")
            }
            if (classRequires.admin()) {
                TestNgUtils.assumeTrue(Settings.ADMIN_AVAILABLE, "XNAT admin account is required for all tests in class: ${testClassName}")
            }
            if (classRequires.email()) {
                TestNgUtils.assumeTrue(Settings.EMAIL_AUTH_VALID, "Email credentials for gmail API are required for all tests in class: ${testClassName}")
            }
            checkPluginSkips(classRequires, true, testClassName)
            if (classRequires.closedXnat()) {
                mainAdminInterface().closeXnat()
            } else if (classRequires.openXnat()) {
                mainAdminInterface().openXnat()
            }
            TestNgUtils.assumeTrue(checkCsBackendsSupported(classRequires), "Could not find an acceptable container backend for all tests in class: ${testClassName}")
            for (final String property : classRequires.trueProperties()) {
                TestNgUtils.assumeTrue(Settings.getBooleanProperty(property, false), "Property \"${property}\" required to be true for all tests in class: ${testClassName}")
            }
            for (final String property : classRequires.falseProperties()) {
                TestNgUtils.assumeFalse(Settings.getBooleanProperty(property, true), "Property \"${property}\" required to be false for all tests in class: ${testClassName}")
            }
        }

        if (requiredUsers > 0) {
            userPool.clear()
            userPool.addAll(createGenericUsers(requiredUsers))
        }
    }

    @BeforeMethod(alwaysRun = true)
    void setupXnatTest(Method m, ITestContext testContext) {
        initializeTestRandomVariables()
        checkTestRequirements(m)
    }

    @AfterClass(alwaysRun = true)
    void restoreSiteConfig() {
        if (siteConfigRestoration == null) {
            log.warn('Object to restore siteConfig is null. Skipping...')
        } else {
            mainAdminInterface().postToSiteConfig(siteConfigRestoration)
        }
    }

    @DataProvider
    static Object[][] backend() {
        Settings.CS_SUPPORTED_BACKENDS.collect { backend ->
            [backend]
        }.toArray(Object[][]::new)
    }

    protected void initializeTestRandomVariables() {
        testSpecificProject = new Project()
        testSpecificSubject = new Subject()
    }

    protected void checkTestRequirements(Method method) {
        final TestRequires testRequires = method.getAnnotation(TestRequires)
        final ExpectedFailure testExpectedFailure = method.getAnnotation(ExpectedFailure)

        if (testRequires != null) {
            final String testName = method.name
            if (testRequires.db()) {
                TestNgUtils.assumeTrue(Settings.HAS_DB_INFO, "DB connection information is required for test: ${testName}")
            }
            if (testRequires.dicomScp()) {
                TestNgUtils.assumeTrue(Settings.HAS_DICOM_RECEIVER_INFO, "DICOM SCP connection information is required for test: ${testName}")
            }
            if (testRequires.ssh()) {
                TestNgUtils.assumeTrue(Settings.SSH_FUNCTIONS, "SSH connection information is required for test: ${testName}")
            }
            if (testRequires.admin()) {
                TestNgUtils.assumeTrue(Settings.ADMIN_AVAILABLE, "XNAT admin account is required for test: ${testName}")
            }
            if (testRequires.email()) {
                TestNgUtils.assumeTrue(Settings.EMAIL_AUTH_VALID, "Email credentials for gmail API are required for test: ${testName}")
            }
            checkPluginSkips(testRequires, false, testName)
            if (testRequires.closedXnat()) {
                mainAdminInterface().closeXnat()
            } else if (testRequires.openXnat()) {
                mainAdminInterface().openXnat()

            }
            TestNgUtils.assumeTrue(checkCsBackendsSupported(testRequires), "Could not find an acceptable container backend for test: ${testName}")
            for (final String property : testRequires.trueProperties()) {
                TestNgUtils.assumeTrue(Settings.getBooleanProperty(property, false), "Property \"${property}\" required to be true for test: ${testName}")
            }
            for (final String property : testRequires.falseProperties()) {
                TestNgUtils.assumeFalse(Settings.getBooleanProperty(property, true), "Property \"${property}\" required to be false for test: ${testName}")
            }
        }

        if (Settings.SKIP_EXPECTED_FAILURE && testExpectedFailure != null) {
            testController.currentTest.setSkipReason(testExpectedFailure.jiraIssue())
            throw new SkipException("Test skipped due to expected failure: ${testExpectedFailure.jiraIssue()}")
        }
    }

    @Override
    protected String readJiraNumber(ITestNGMethod test) {
        final JiraKey key = TestNgUtils.getAnnotation(test, JiraKey)
        if (key != null) {
            final String simpleId = key.simpleKey()
            final XnatVersionLink[] versionLinks = key.versionMap()

            if (containsCurrentXnatVersion(versionLinks)) {
                getJiraId(versionLinks)
            } else if (StringUtils.isNotEmpty(simpleId)) {
                simpleId
            } else {
                null
            }
        } else {
            null
        }
    }

    private String getJiraId(XnatVersionLink[] links) {
        links.find { link ->
            link.xnatVersions().any { xnatVersion ->
                xnatVersion == Settings.XNAT_VERSION
            }
        }.mappedValue()
    }

    private boolean containsCurrentXnatVersion(XnatVersionLink[] links) {
        links.any { link ->
            link.xnatVersions().any { xnatVersion ->
                xnatVersion == Settings.XNAT_VERSION
            }
        }
    }

    private void noteInitialConfigSettings() {
        if (Settings.ADMIN_AVAILABLE) {
            final SiteConfig existingConfig = mainAdminInterface().readSiteConfig()
            siteConfigRestoration = new SiteConfig(
                    requireLogin: existingConfig.requireLogin,
                    requireEmailVerification: existingConfig.requireEmailVerification,
                    autoEnableUsers: existingConfig.autoEnableUsers,
                    preventCrossModalityMerge: existingConfig.preventCrossModalityMerge,
                    restrictUserListToAdmins: existingConfig.restrictUserListToAdmins,
                    dicomFileNameTemplate: existingConfig.dicomFileNameTemplate
            )
        }
    }

    /**
     * Takes care of all requirements that may be needed for individual tests/classes but that can be satisfied on startup
     */
    private void handleSetupAnnotationRequirements() {
        final Set<TestData> testDataRequirements = []
        allTests.each { test ->
            final List<TestRequires> requirements = [TestNgUtils.getAnnotation(test, TestRequires), test.realClass.getAnnotation(TestRequires)]
            requirements.each { requirement ->
                if (requirement != null) {
                    testDataRequirements.addAll(requirement.data())
                }
            }
        }
        testDataRequirements.each { testData ->
            testData.download()
        }
    }

    protected List<XnatPlugin> installedPlugins() {
        if (installedPlugins.isEmpty()) {
            installedPlugins.addAll(mainAdminInterface().readInstalledPlugins())
        }
        installedPlugins
    }

    protected XnatPlugin getInstalledPlugin(final String pluginId) {
        installedPlugins()
                .find { plugin -> plugin.id == pluginId } ?:
                { throw new IllegalStateException("Unable to find plugin " + pluginId) }()
    }

    protected Version getPluginVersion(String pluginId) {
        new Version(getInstalledPlugin(pluginId))
    }


    protected abstract void setupXnat()

    protected abstract List<User> createGenericUsers(int numUsers)

    protected void validateSettings() {
        [
                (XNATProperties.MAIN_USER[0]) : Settings.MAIN_USERNAME,
                (XNATProperties.MAIN_PASS[0]) : Settings.MAIN_PASS,
                (XNATProperties.BASEURL) : Settings.BASEURL
        ].each { propertyName, settingValue ->
            if (settingValue == null) {
                throw new RuntimeException("Required value found to be null. Property must be set as ${propertyName}.")
            }
        }

        if (Settings.ADMIN_SETTING) {
            checkConditionalProperties(XNATProperties.ADMIN_SETTING, [
                    (XNATProperties.MAIN_ADMIN_USER[0]) : Settings.MAIN_ADMIN_USERNAME,
                    (XNATProperties.MAIN_ADMIN_PASS[0]) : Settings.MAIN_ADMIN_PASS,
                    (XNATProperties.ADMIN_USER) : Settings.ADMIN_USERNAME,
                    (XNATProperties.ADMIN_PASS) : Settings.ADMIN_PASS
            ])
        }
        if (Settings.NOTIFICATION_EMAILS != null) {
            checkConditionalProperties(XNATProperties.NOTIFICATION_EMAILS, [
                    (XNATProperties.EMAIL) : Settings.EMAIL
            ])
        }
        if (Settings.JIRA_SETTING) {
            checkConditionalProperties(XNATProperties.JIRA_SETTING, [
                    (JIRAProperties.JIRA_USER) : JIRASettings.JIRA_USER,
                    (JIRAProperties.JIRA_PASS) : JIRASettings.JIRA_PASS,
                    (JIRAProperties.JIRA_URL) : JIRASettings.JIRA_URL,
                    (JIRAProperties.JIRA_PROJECT) : JIRASettings.PROJECT,
                    (JIRAProperties.JIRA_PROJECT_VERSION) : JIRASettings.VERSION
            ])
        }

        if (Settings.DYNAMIC_ORDERING) {
            if (Settings.QUEUE_SLOTS == 0){
                throw new RuntimeException("Dynamic ordering is being used, so ${XNATProperties.QUEUE_SLOTS} must be set to a positive integer number of slots.")
            }
        }

        if (Settings.INIT_SETTING) {
            checkDependentBooleans(XNATProperties.INIT_SETTING, XNATProperties.ADMIN_SETTING, Settings.ADMIN_SETTING)
        }
        if (Settings.PRODUCE_PDF) {
            checkDependentBooleans(XNATProperties.PRODUCE_PDF, XNATProperties.JIRA_SETTING, Settings.JIRA_SETTING)
        }
    }

    // Used when baseOptionalPropertyName being specified requires downstream properties to be set
    private void checkConditionalProperties(String baseOptionalPropertyName, Map<String, String> propertySettingMap) {
        propertySettingMap.each { propertyName, settingValue ->
            if (settingValue == null) {
                throw new RuntimeException("${baseOptionalPropertyName} is set to true, which means that property ${propertyName} must also be provided.")
            }
        }
    }

    private void checkDependentBooleans(String baseOptionalBooleanPropertyName, String propertyName, boolean settingValue) {
        if (!settingValue) {
            throw new RuntimeException("${baseOptionalBooleanPropertyName} is set to true, which means that ${propertyName} must also be.")
        }
    }

    protected User getGenericUser() {
        if (userPool.size() > 0) {
            userPool.remove(0)
        } else {
            null
        }
    }

    protected void constructRestDriver(XnatConfig xnatConfig = null) {
        restDriver = XnatRestDriver.getInstance(xnatConfig)
        if (testController == null) {
            testController = new TestController()
        }
        restDriver.setTestController(testController)
    }

    XnatInterface mainInterface() {
        interfaceFor(mainUser)
    }

    XnatInterface mainAdminInterface() {
        interfaceFor(mainAdminUser)
    }

    protected XnatInterface interfaceFor(User user) {
        final XnatInterface xnatInterface = restDriver.interfaceFor(user)
        additionalRegisteredSubinterfaces().each { subinterfaceClass ->
            xnatInterface.registerExternalSubinterface(subinterfaceClass)
        }
        xnatInterface
    }

    protected List<Class<? extends XnatFunctionalitySubinterface>> additionalRegisteredSubinterfaces() {
        []
    }

    private XnatRestDriver initRestDriver() { // Just to guarantee restDriver object is properly initialized if subclasses want to use it via instance variables
        constructRestDriver()
        restDriver
    }

    private void checkPluginSkips(TestRequires testRequires, boolean isClassLevel, String testOrClass) {
        if (Settings.BEHAVIOR_FOR_MISSING_PLUGIN == TestBehavior.SKIP) {
            final String scope = isClassLevel ? 'all tests in class' : 'test'
            PluginDependencyManager.parsePluginRequirements(testRequires).each { pluginRequirement ->
                final PluginDependencyCheck pluginDependencyCheck = PluginDependencyManager.checkPlugin(installedPlugins(), pluginRequirement)
                switch (pluginDependencyCheck.state) {
                    case PluginDependencyCheckState.MISSING_PLUGIN:
                        throw new SkipException("XNAT plugin with id ${pluginRequirement.pluginId} is required for ${scope}: ${testOrClass}")
                    case PluginDependencyCheckState.VERSION_MISMATCH:
                        throw new SkipException("XNAT plugin with id ${pluginRequirement.pluginId} is required for ${scope}: ${testOrClass}. We found the plugin on the XNAT instance, but without a compatible version: ${pluginDependencyCheck.failureReason}.")
                }
            }
        }
    }

    private boolean checkCsBackendsSupported(TestRequires testRequires) {
        if (testRequires.supportedContainerBackends().size() == 0) {
            true
        } else {
            testRequires.supportedContainerBackends().any { testCompatibleBackend ->
                Settings.CS_SUPPORTED_BACKENDS.contains(testCompatibleBackend)
            }
        }
    }

    XnatRestDriver getRestDriver() {
        restDriver
    }

}
