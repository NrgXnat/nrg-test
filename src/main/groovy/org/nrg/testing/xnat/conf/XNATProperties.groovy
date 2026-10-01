package org.nrg.testing.xnat.conf

import org.apache.commons.lang3.StringUtils
import org.apache.log4j.Logger
import org.nrg.testing.CommonStringUtils
import org.nrg.testing.enums.TestBehavior
import org.nrg.testing.util.BaseProperties
import org.nrg.xnat.pogo.containers.Backend
import org.nrg.xnat.versions.XnatVersion
import org.nrg.xnat.versions.XnatVersionList

class XNATProperties extends BaseProperties {

    private static final Logger LOGGER = Logger.getLogger(XNATProperties)
    public static final List<String> MAIN_USER = seleniumAliasedProperty('xnat.main.user')
    public static final List<String> MAIN_PASS = seleniumAliasedProperty('xnat.main.password')
    public static final List<String> MAIN_ADMIN_USER = seleniumAliasedProperty('xnat.mainAdmin.user')
    public static final List<String> MAIN_ADMIN_PASS = seleniumAliasedProperty('xnat.mainAdmin.password')
    public static final String ADMIN_USER = 'xnat.admin.user'
    public static final String ADMIN_PASS = 'xnat.admin.password'
    public static final String XNAT_VERSION = 'xnat.version'
    public static final String BASEURL = 'xnat.baseurl'
    public static final String EMAIL = 'xnat.users.email'
    @Deprecated public static final String EMAIL_PASS = 'xnat.users.email.password'
    public static final String EMAIL_TOKENS_DIR = 'xnat.users.email.tokens'
    public static final String EMAIL_CREDENTIALS = 'xnat.users.email.credentials'
    public static final String DEFAULT_TIMEOUT = 'xnat.defaultTimeout'
    public static final String BROWSER = 'xnat.browser'
    public static final String INIT_SETTING = 'xnat.init'
    public static final String TEMP_DIR = 'xnat.temp'
    public static final String DICOM_HOST = 'xnat.dicom.host'
    public static final String DICOM_PORT = 'xnat.dicom.port'
    public static final String DICOM_AETITLE = 'xnat.dicom.aetitle'
    public static final String CALLING_AE_TITLE = 'xnat.dicom.callingaetitle'
    public static final String JIRA_SETTING = 'xnat.jira'
    public static final String ADMIN_SETTING = 'xnat.requireAdmin'
    public static final String DYNAMIC_ORDERING = 'xnat.pipeline.useDynamicOrdering'
    public static final String QUEUE_SLOTS = 'xnat.pipeline.slots'
    public static final String NOTIFICATION_EMAILS = 'xnat.notifiedEmails'
    public static final String NOTIFICATION_SETTING = 'xnat.notifyOnSuccess'
    public static final String NOTIFICATION_TITLE = 'xnat.notificationTitle'
    public static final String CHECK_DEPENDENCIES = 'xnat.dependencies'
    public static final String TIMELOG_SETTING = 'xnat.timelogs'
    public static final String GITLOGS_SETTING = 'xnat.gitLogs'
    public static final String BASIC_MODE = 'xnat.basic'
    public static final String LOGGING_SETTING = 'xnat.allowLogging'
    public static final String DATABASE_URL = 'xnat.db.url'
    public static final String DATABASE_USER = 'xnat.db.user'
    public static final String DATABASE_PASS = 'xnat.db.password'
    public static final String SSH_USER = 'xnat.ssh.user'
    public static final String SSH_PRIVATE_KEY_NAME = 'xnat.ssh.key'
    public static final String SSH_SKIP_HOST_KEY_VERIFICATION = 'xnat.ssh.skipHostKeyVerification'
    public static final String PRODUCE_PDF = 'xnat.producePdf'
    public static final String DOM_SETTING = 'xnat.captureDom'
    public static final String SETUP_MRSCAN = 'xnat.setupMrscan'
    public static final String EXPECTED_FAILURE_BEHAVIOR = 'xnat.testBehavior.expectedFailures'
    public static final String MISSING_PLUGIN_BEHAVIOR = 'xnat.testBehavior.missingPlugins'
    public static final String FIREFOX_BINARY_PATH = 'firefox.path'
    public static final String TOMCAT_CONTROL_SCRIPT = 'tomcat.control'
    public static final String SELENIUM_HEADLESS = 'selenium.headless'
    public static final String XNAT_REQUIRED = 'xnat.required' // used to specify that additional XNATs will be needed
    public static final String SMTP_HOST = 'mail.smtp.host'
    public static final String SMTP_PORT = 'mail.smtp.port'
    public static final String CS_SWARM_TIMEOUT = 'cs.swarm.timeout'
    public static final String CS_SWARM_CONSTRAINTS = 'cs.swarm.constraints'
    public static final String CS_KUBERNETES_TOLERATIONS = 'cs.kubernetes.tolerations'
    public static final String CS_SUPPORTED_BACKENDS = 'cs.backends'
    public static final String ALLOW_PERFORMANCE_TESTS = 'xnat.performance.allow'
    public static final String PERFORMANCE_RESET_SCRIPT_ID = 'xnat.performance.resetId'
    public static final String PERFORMANCE_DEPLOYMENTS = 'xnat.performance.deployments'
    public static final String PERFORMANCE_INSTALL_PLUGIN_SCRIPT = 'xnat.performance.plugin.install'
    public static final String PERFORMANCE_UNINSTALL_PLUGIN_SCRIPT = 'xnat.performance.plugin.uninstall'
    public static final String PERFORMANCE_SET_BASELINE = 'xnat.performance.setBaseline'
    public static final String PERFORMANCE_EXPORT_ONLY = 'xnat.performance.exportOnly'
    public static final String PERFORMANCE_NEW_TESTS_ONLY = 'xnat.performance.newTestsOnly'
    public static final String PERFORMANCE_COMPILE_PDF = 'xnat.performance.compilePdf'
    public static final String PERFORMANCE_PLATFORM = 'xnat.performance.platform'
    public static final String PERFORMANCE_SERVER_CONTROL = 'xnat.performance.serverControl'
    public static final String KUBERNETES_KUBECTL = 'xnat.k8s.kubectl'
    public static final String KUBERNETES_CONTEXT = 'xnat.k8s.context'
    public static final String KUBERNETES_NAMESPACE = 'xnat.k8s.namespace'
    public static final String KUBERNETES_WORKLOAD = 'xnat.k8s.workload'
    public static final String KUBERNETES_POD = 'xnat.k8s.pod'
    public static final String KUBERNETES_CONTAINER = 'xnat.k8s.container'
    public static final String KUBERNETES_CONTAINERS = 'xnat.k8s.containers'
    public static final String KUBERNETES_DB_POD = 'xnat.k8s.db.pod'
    public static final String KUBERNETES_DB_CONTAINER = 'xnat.k8s.db.container'
    public static final String KUBERNETES_DB_NAME = 'xnat.k8s.db.name'
    public static final String KUBERNETES_DB_USER = 'xnat.k8s.db.user'
    public static final String KUBERNETES_DATA_PATHS = 'xnat.k8s.dataPaths'
    public static final String KUBERNETES_PLUGINS_DIR = 'xnat.k8s.pluginsDir'
    public static final String KUBERNETES_PLUGINS_SOURCE = 'xnat.k8s.pluginsSource'
    public static final String KUBERNETES_IMAGE = 'xnat.k8s.image'
    public static final String KUBERNETES_LOGS_DIR = 'xnat.k8s.logsDir'
    public static final String KUBERNETES_SAVE_LOGS_TO = 'xnat.k8s.saveLogsTo'
    public static final String KUBERNETES_STARTUP_TIMEOUT = 'xnat.k8s.startupTimeout'
    public static final String KUBERNETES_RESET_CONFIRM = 'xnat.k8s.reset.confirm'
    public static final String KUBERNETES_VERIFY_VERSION = 'xnat.k8s.verifyVersion'
    public static final String DQR_PACS_DIMSE_AE_TITLE = 'dqr.pacs.dimse.aeTitle'
    public static final String DQR_PACS_DIMSE_HOST = 'dqr.pacs.dimse.host'
    public static final String DQR_PACS_DIMSE_PORT = 'dqr.pacs.dimse.port'
    public static final String DQR_PACS_DICOMWEB_AE_TITLE = 'dqr.pacs.dicomweb.aeTitle'
    public static final String DQR_PACS_DICOMWEB_ROOT_URL = 'dqr.pacs.dicomweb.rootUrl'
    public static final String DQR_SCP_RECEIVER_AE_TITLE = 'dqr.scpReceiver.aeTitle'
    public static final String DQR_SCP_RECEIVER_PORT = 'dqr.scpReceiver.port'

    XNATProperties() {
        super('xnat.config', 'local.properties')
    }

    private String nthXnatProperty(String basePropertyName, int n) {
        basePropertyName.replace('xnat.', "xnat${n}.")
    }

    private static List<String> seleniumAliasedProperty(String normalProperty) {
        [normalProperty, normalProperty.replace('main', 'selenium')]
    }

    String getBaseURL() {
        getPropertyFromAnywhere(BASEURL)
    }

    String getAdminUser() {
        getPropertyFromAnywhere(ADMIN_USER)
    }

    String getAdminPassword() {
        getSensitiveProperty(ADMIN_PASS)
    }

    String getMainUser() {
        getPropertyFromAnywhere(MAIN_USER)
    }

    String getMainPassword() {
        getSensitiveProperty(MAIN_PASS)
    }

    String getMainEmail() {
        getPropertyFromAnywhere(EMAIL)
    }

    @Deprecated String getMainEmailPassword() {
        getSensitiveProperty(EMAIL_PASS)
    }

    String getEmailTokensDir() {
        getStringProperty(EMAIL_TOKENS_DIR, 'tokens')
    }

    String getEmailCredentialsJson() {
        getPropertyFromAnywhere(EMAIL_CREDENTIALS)
    }

    String getMainAdminUser() {
        getPropertyFromAnywhere(MAIN_ADMIN_USER)
    }

    String getMainAdminPassword() {
        getSensitiveProperty(MAIN_ADMIN_PASS)
    }

    int getDefaultTimeout() {
        getIntProperty(DEFAULT_TIMEOUT, 30)
    }

    String getBrowser() {
        getStringProperty(false, BROWSER, 'Firefox')
    }

    boolean getInitSetting() {
        getBooleanProperty(INIT_SETTING, true)
    }

    String getTempFolder() {
        getPropertyFromAnywhere(TEMP_DIR) ?: StringUtils.stripEnd(System.getProperty('java.io.tmpdir'), File.separator)
    }

    String getDicomHost() {
        getPropertyFromAnywhere(DICOM_HOST) ?: new URL(Settings.BASEURL).host
    }

    int getDicomPort() {
        getIntProperty(DICOM_PORT, 8104)
    }

    String getDicomAetitle() {
        getPropertyFromAnywhere(DICOM_AETITLE) ?: 'XNAT'
    }

    String getCallingAETitle() {
        getPropertyFromAnywhere(CALLING_AE_TITLE) ?: 'GRXNAT'
    }

    boolean getJiraSetting() {
        getBooleanProperty(JIRA_SETTING, false)
    }

    boolean getAdminSetting() {
        getBooleanProperty(ADMIN_SETTING, true)
    }

    boolean getDynamicOrderingSetting() {
        getBooleanProperty(DYNAMIC_ORDERING, false)
    }

    int getQueueSlots() {
        getIntProperty(QUEUE_SLOTS, 0)
    }

    String[] getNotificationEmails() {
        final String emailString = getPropertyFromAnywhere(NOTIFICATION_EMAILS)
        StringUtils.isEmpty(emailString) ? null : emailString.split(',')
    }

    boolean getNotificationSetting() {
        getBooleanProperty(NOTIFICATION_SETTING, false)
    }

    String getNotificationTitle() {
        getPropertyFromAnywhere(NOTIFICATION_TITLE)
    }

    boolean getDependencyCheck() {
        getBooleanProperty(CHECK_DEPENDENCIES, true)
    }

    boolean getTimelogSetting() {
        getBooleanProperty(TIMELOG_SETTING, false)
    }

    boolean getGitlogSetting() {
        getBooleanProperty(GITLOGS_SETTING, false)
    }

    boolean getBasicSetting() {
        getBooleanProperty(BASIC_MODE, false)
    }

    boolean getLoggingAllowedSetting() {
        getBooleanProperty(LOGGING_SETTING, true)
    }

    String getDatabaseUrl() {
        getSensitiveProperty(DATABASE_URL)
    }

    String getDatabaseUser() {
        getSensitiveProperty(DATABASE_USER)
    }

    String getDatabasePass() {
        getSensitiveProperty(DATABASE_PASS)
    }

    String getSshUser() {
        getPropertyFromAnywhere(SSH_USER)
    }

    String getSshPrivateKeyName() {
        getStringProperty(false, SSH_PRIVATE_KEY_NAME, 'id_rsa')
    }

    boolean getSshSkipHostKeyVerificationSetting() {
        getBooleanProperty(SSH_SKIP_HOST_KEY_VERIFICATION, false)
    }

    boolean getPdfSetting() {
        getBooleanProperty(PRODUCE_PDF, false)
    }

    boolean getDomSetting() {
        getBooleanProperty(DOM_SETTING, false)
    }

    boolean getMrScanSetupSetting() {
        getBooleanProperty(SETUP_MRSCAN, false)
    }

    TestBehavior getExpectedFailureTestBehavior() {
        final String propertyValue = getStringProperty(false, EXPECTED_FAILURE_BEHAVIOR, TestBehavior.SKIP.identifier)
        final TestBehavior behavior = TestBehavior.get(propertyValue)
        if (behavior == null) {
            throw new RuntimeException("Unknown value '${propertyValue}' for ${EXPECTED_FAILURE_BEHAVIOR}. Supported values: ${TestBehavior.values()*.identifier}.")
        } else {
            behavior
        }
    }

    TestBehavior getMissingPluginTestBehavior() {
        final String propertyValue = getStringProperty(false, MISSING_PLUGIN_BEHAVIOR, TestBehavior.IGNORE.identifier)
        final TestBehavior behavior = TestBehavior.get(propertyValue)
        if (behavior == null || behavior == TestBehavior.RUN) {
            throw new RuntimeException("Unknown value '${propertyValue}' for ${MISSING_PLUGIN_BEHAVIOR}. Supported values: [${TestBehavior.IGNORE.identifier}, ${TestBehavior.SKIP.identifier}].")
        } else {
            behavior
        }
    }

    @Deprecated
    int getCsSwarmTimeout() {
        getIntProperty(CS_SWARM_TIMEOUT, 5)
    }

    Class<? extends XnatVersion> getXNATVersion() {
        parseVersion(XNAT_VERSION)
    }

    String getXnatVersionString() {
        getPropertyFromAnywhere(XNAT_VERSION)
    }

    String getTomcatControlScriptKey() {
        getStringProperty(false, TOMCAT_CONTROL_SCRIPT, null)
    }

    String getFirefoxPath() {
        getStringProperty(false, FIREFOX_BINARY_PATH, null)
    }

    String getSmtpHost() {
        getStringProperty(false, SMTP_HOST, 'localhost')
    }

    int getSmtpPort() {
        getIntProperty(SMTP_PORT, 25)
    }

    boolean getHeadlessSetting() {
        getBooleanProperty(SELENIUM_HEADLESS, true)
    }

    boolean nthXnatRequired(int n) {
        getBooleanProperty(nthXnatProperty(XNAT_REQUIRED, n), false)
    }

    String getSwarmConstraints() {
        getPropertyFromAnywhere(CS_SWARM_CONSTRAINTS)
    }

    String getKubernetesTolerations() {
        getPropertyFromAnywhere(CS_KUBERNETES_TOLERATIONS)
    }

    List<Backend> getSupportedContainerBackends() {
        getStringProperty(CS_SUPPORTED_BACKENDS, Backend.DOCKER.lower())
                .split(',')
                .collect { stringRep ->
                    Backend.fromString(stringRep)
                }
    }

    Backend getPreferredBackend() {
        getSupportedContainerBackends()[0]
    }

    boolean getPerformanceAllowedSetting() {
        getBooleanProperty(ALLOW_PERFORMANCE_TESTS, false)
    }

    String getPerformanceResetScriptId() {
        getStringProperty(PERFORMANCE_RESET_SCRIPT_ID, kubernetesPlatformDefault())
    }
    
    String getPerformanceDeployments() {
        getStringProperty(PERFORMANCE_DEPLOYMENTS, null)
    }

    String getPerformancePluginInstallId() {
        getStringProperty(PERFORMANCE_INSTALL_PLUGIN_SCRIPT, kubernetesPlatformDefault())
    }

    String getPerformancePluginUninstallId() {
        getStringProperty(PERFORMANCE_UNINSTALL_PLUGIN_SCRIPT, kubernetesPlatformDefault())
    }

    /** {@code ssh} (the default) or {@code kubernetes}; the default for the server control, reset and plugin keys. */
    String getPerformancePlatform() {
        getStringProperty(PERFORMANCE_PLATFORM, 'ssh')
    }

    String getPerformanceServerControlId() {
        getStringProperty(PERFORMANCE_SERVER_CONTROL, performancePlatform)
    }

    private String kubernetesPlatformDefault() {
        performancePlatform == 'kubernetes' ? 'kubernetes' : null
    }

    String getKubernetesKubectl() {
        getStringProperty(KUBERNETES_KUBECTL, 'kubectl')
    }

    String getKubernetesContext() {
        getStringProperty(KUBERNETES_CONTEXT, null)
    }

    String getKubernetesNamespace() {
        getStringProperty(KUBERNETES_NAMESPACE, null)
    }

    String getKubernetesWorkload() {
        getStringProperty(KUBERNETES_WORKLOAD, 'statefulset/xnat')
    }

    /** The XNAT pod, or null to use the StatefulSet's {@code <name>-0}. */
    String getKubernetesPod() {
        getStringProperty(KUBERNETES_POD, null)
    }

    String getKubernetesContainer() {
        getStringProperty(KUBERNETES_CONTAINER, null)
    }

    List<String> getKubernetesContainers() {
        getStringProperty(KUBERNETES_CONTAINERS, 'xnat,home-init').split(',')*.trim().findAll()
    }

    String getKubernetesDatabasePod() {
        getStringProperty(KUBERNETES_DB_POD, null)
    }

    String getKubernetesDatabaseContainer() {
        getStringProperty(KUBERNETES_DB_CONTAINER, null)
    }

    String getKubernetesDatabaseName() {
        getStringProperty(KUBERNETES_DB_NAME, 'xnat')
    }

    String getKubernetesDatabaseUser() {
        getStringProperty(KUBERNETES_DB_USER, null)
    }

    List<String> getKubernetesDataPaths() {
        final String paths = getStringProperty(KUBERNETES_DATA_PATHS, null)
        paths ? paths.split(',')*.trim().findAll() : null
    }

    String getKubernetesPluginsDirectory() {
        getStringProperty(KUBERNETES_PLUGINS_DIR, '/data/xnat/home/plugins')
    }

    String getKubernetesPluginsSource() {
        getStringProperty(KUBERNETES_PLUGINS_SOURCE, null)
    }

    String getKubernetesImage() {
        getStringProperty(KUBERNETES_IMAGE, null)
    }

    String getKubernetesLogsDirectory() {
        getStringProperty(KUBERNETES_LOGS_DIR, '/data/xnat/home/logs')
    }

    String getKubernetesSaveLogsTo() {
        getStringProperty(KUBERNETES_SAVE_LOGS_TO, 'target/xnat-logs')
    }

    int getKubernetesStartupTimeout() {
        getIntProperty(KUBERNETES_STARTUP_TIMEOUT, 900)
    }

    String getKubernetesResetConfirmation() {
        getStringProperty(KUBERNETES_RESET_CONFIRM, null)
    }

    boolean getKubernetesVerifyVersion() {
        getBooleanProperty(KUBERNETES_VERIFY_VERSION, true)
    }

    boolean getPerformanceSetBaselineSetting() {
        getBooleanProperty(PERFORMANCE_SET_BASELINE, false)
    }

    boolean getPerformanceNewTestsOnlySetting() {
        getBooleanProperty(PERFORMANCE_NEW_TESTS_ONLY, false)
    }

    boolean getPerformanceExportOnlySetting() {
        getBooleanProperty(PERFORMANCE_EXPORT_ONLY, false)
    }

    boolean getPerformanceCompilePdfSetting() {
        getBooleanProperty(PERFORMANCE_COMPILE_PDF, false)
    }

    String getDqrPacsDimseAeTitle() {
        getStringProperty(DQR_PACS_DIMSE_AE_TITLE, null)
    }

    String getDqrPacsDimseHost() {
        getStringProperty(DQR_PACS_DIMSE_HOST, null)
    }

    int getDqrPacsDimsePort() {
        getIntProperty(DQR_PACS_DIMSE_PORT, 4242)
    }

    String getDqrPacsDicomwebAeTitle() {
        getStringProperty(DQR_PACS_DICOMWEB_AE_TITLE, null)
    }

    String getDqrPacsDicomwebRootUrl() {
        getStringProperty(DQR_PACS_DICOMWEB_ROOT_URL, null)
    }

    String getDqrScpReceiverAeTitle() {
        getStringProperty(DQR_SCP_RECEIVER_AE_TITLE, null)
    }

    int getDqrScpReceiverPort() {
        getIntProperty(DQR_SCP_RECEIVER_PORT, 8104)
    }
    List<XnatConfig> getOtherXnatConfigs() {
        final List<XnatConfig> bonusConfigs = []
        int configIndex = 2
        while (true) {
            if (nthXnatRequired(configIndex)) {
                bonusConfigs << new XnatConfig().
                        mainUsername(getPropertyFromAnywhere(nthXnatProperty(MAIN_USER[0], configIndex))).
                        mainPassword(getSensitiveProperty(nthXnatProperty(MAIN_PASS[0], configIndex))).
                        mainAdminUsername(getPropertyFromAnywhere(nthXnatProperty(MAIN_ADMIN_USER[0], configIndex))).
                        mainAdminPassword(getSensitiveProperty(nthXnatProperty(MAIN_ADMIN_PASS[0], configIndex))).
                        adminUsername(getPropertyFromAnywhere(nthXnatProperty(ADMIN_USER, configIndex))).
                        adminPassword(getSensitiveProperty(nthXnatProperty(ADMIN_PASS, configIndex))).
                        xnatVersion(parseVersion(nthXnatProperty(XNAT_VERSION, configIndex))).
                        xnatUrl(CommonStringUtils.formatUrl(getPropertyFromAnywhere(nthXnatProperty(BASEURL, configIndex)))).
                        init(getBooleanProperty(nthXnatProperty(INIT_SETTING, configIndex), true)).
                        build()
            } else {
                break
            }
            configIndex++
        }
        bonusConfigs
    }

    private Class<? extends XnatVersion> parseVersion(String key) {
        final String version = getPropertyFromAnywhere(key)
        if (version == null) {
            LOGGER.fatal("Required setting ${key} was not set.")
            throw new RuntimeException("Required setting ${key} was not set.")
        }

        XnatVersionList.lookup(version)
    }

}