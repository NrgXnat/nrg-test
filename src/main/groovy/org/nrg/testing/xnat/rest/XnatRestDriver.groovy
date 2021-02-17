package org.nrg.testing.xnat.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.restassured.http.ContentType
import com.jayway.restassured.internal.RestAssuredResponseImpl
import com.jayway.restassured.path.json.exception.JsonPathException
import com.jayway.restassured.response.Response
import com.jayway.restassured.specification.RequestSender
import com.jayway.restassured.specification.RequestSpecification
import groovy.util.logging.Log4j
import org.apache.commons.lang3.time.StopWatch
import org.nrg.jira.components.zephyr.TestStatus
import org.nrg.testing.CommonStringUtils
import org.nrg.testing.HttpUtils
import org.nrg.testing.TestController
import org.nrg.testing.TestNgUtils
import org.nrg.testing.TimeUtils
import org.nrg.testing.enums.TestData
import org.nrg.testing.util.RandomHelper
import org.nrg.testing.xnat.XnatObjectUtils
import org.nrg.testing.xnat.conf.Settings
import org.nrg.testing.xnat.conf.XnatConfig
import org.nrg.testing.xnat.versions.XnatTestingVersionManager
import org.nrg.xnat.enums.Accessibility
import org.nrg.xnat.enums.DicomEditVersion
import org.nrg.xnat.enums.PrearchiveCode
import org.nrg.xnat.enums.RoutingRulesType
import org.nrg.xnat.interfaces.XnatInterface
import org.nrg.xnat.pogo.*
import org.nrg.xnat.pogo.experiments.*
import org.nrg.xnat.pogo.extensions.project.ProjectXMLPutExtension
import org.nrg.xnat.pogo.extensions.subject.SubjectExtension
import org.nrg.xnat.pogo.extensions.subject.SubjectXMLPutExtension
import org.nrg.xnat.pogo.resources.Resource
import org.nrg.xnat.pogo.resources.ResourceFile
import org.nrg.xnat.pogo.users.User
import org.nrg.xnat.pogo.users.UserGroup
import org.nrg.xnat.rest.Credentials
import org.nrg.xnat.rest.XnatAliasToken
import org.nrg.xnat.versions.XnatVersion

import java.nio.file.Paths

import static com.jayway.restassured.http.ContentType.JSON
import static org.hamcrest.CoreMatchers.equalTo

@SuppressWarnings('unused')
@Log4j
abstract class XnatRestDriver {

    @Delegate(includes = [
            'getMainCredentials',
            'getMainAdminCredentials',
            'getAdminCredentials',
            'getMainUser',
            'getMainAdminUser',
            'getAdminUser',
            'getMainUsername',
            'getMainAdminUsername',
            'getAdminUsername',
            'getMainPassword',
            'getMainAdminPassword',
            'getAdminPassword',
            'getXnatUrl'
    ]) XnatConfig xnatConfig
    TestController testController
    protected static final Map<User, XnatInterface> xnatInterfaceMap = [:]
    public static final ObjectMapper XNAT_REST_MAPPER = XnatInterface.XNAT_REST_MAPPER

    abstract List<Class<? extends XnatVersion>> getHandledVersions()

    static XnatRestDriver getInstance(XnatConfig xnatConfig) {
        final XnatConfig config = xnatConfig ?: Settings.DEFAULT_XNAT_CONFIG
        final XnatRestDriver restDriver = XnatTestingVersionManager.lookupDriverClass(config.xnatVersion).newInstance()
        restDriver.setXnatConfig(config)
        restDriver
    }

    static XnatRestDriver getInstance() {
        getInstance(Settings.DEFAULT_XNAT_CONFIG)
    }

    static invalidateCachedCredentials() {
        xnatInterfaceMap.values().each { xnatInterface ->
            xnatInterface.removeCachedAuth()
        }
    }

    XnatInterface interfaceFor(User user) {
        final XnatInterface xnatInterface = xnatInterfaceMap[user]
        if (xnatInterface != null) {
            xnatInterface
        } else {
            final XnatInterface newInterface = XnatInterface.authenticate(xnatConfig.xnatUrl, user, xnatConfig.xnatVersion, false)
            xnatInterfaceMap.put(user, newInterface)
            newInterface
        }
    }

    XnatInterface mainInterface() {
        interfaceFor(mainUser)
    }

    RequestSpecification mainQueryBase() {
        mainInterface().queryBase()
    }

    RequestSpecification queryBaseFor(User user) {
        interfaceFor(user).queryBase()
    }

    void captureStep(TestStatus status, String comment) {
        if (testController.testRunning) {
            testController.currentTest.updateStepResult(testController.stepCounter.value, status, comment)
            testController.stepCounter.increment()
        }
    }

    void passStep(String comment) {
        captureStep(TestStatus.PASS, comment)
    }

    void passStep() {
        passStep(null)
    }

    String formatXnatUrl(String... components) {
        CommonStringUtils.formatUrl(xnatConfig.xnatUrl, CommonStringUtils.formatUrl(components))
    }

    String formatRestUrl(String... components) {
        formatXnatUrl('data', CommonStringUtils.formatUrl(components))
    }

    String formatXapiUrl(String... components) {
        formatXnatUrl('xapi', CommonStringUtils.formatUrl(components))
    }

    Response getJson(RequestSender request, String url) {
        // request should be a RequestSpecification if it's just credentials or a ResponseSpecification if it's credentials appended with expected response behavior
        final Response response = request.get(url)
        fixContentType(response, JSON)
        response
    }

    void fixContentType(Response restResponse, ContentType type) {
        (restResponse as RestAssuredResponseImpl).setContentType(type.toString()) // XNAT is returning the wrong content type in some cases
    }

    File saveBinaryResponseToFile(Response response) {
        final File downloadedFile = Paths.get(Settings.TEMP_SUBDIR, "${RandomHelper.randomID()}.binarytestfile").toFile()
        HttpUtils.saveBinaryResponseToFile(response, downloadedFile)
        downloadedFile
    }

    RequestSpecification invalidCredentials() {
        Credentials.build(RandomHelper.randomLetters(12), RandomHelper.randomLetters(12)) // randomly generating this is fine. Probability of collision is astronomically small with 12 letters
    }

    String aliasTokenUrl() {
        mainInterface().issueAliasTokenUrl()
    }

    @Deprecated
    XnatAliasToken generateAliasToken(User user) {
        interfaceFor(user).generateAliasToken()
    }

    @Deprecated
    List<XnatPlugin> readInstalledPlugins(User user) {
        interfaceFor(user).readInstalledPlugins()
    }

    AnonScript getDefaultXnatAnonScript() {
        XnatObjectUtils.anonScriptFromURL(DicomEditVersion.UNSPECIFIED, formatXapiUrl('anonymize/default'), Settings.DEFAULT_XNAT_CONFIG.adminUser)
    }
    
    String projectExperimentsUrl(Project project) {
        mainInterface().projectExperimentsUrl(project)
    }

    @Deprecated
    String getAccessionNumber(User authUser, SubjectAssessor subjectAssessor) {
        interfaceFor(authUser).getAccessionNumber(subjectAssessor)
    }

    String getAccessionNumber(ImagingSession session) {
        getAccessionNumber(mainUser, session)
    }

    @Deprecated
    Subject readSubject(User authUser, String accessionNumber) {
        interfaceFor(authUser).readSubject(accessionNumber)
    }

    @Deprecated
    def <T extends Experiment> T readExperiment(User authUser, String accessionNumber, Class<T> tClass) {
        interfaceFor(authUser).readExperiment(accessionNumber, tClass)
    }

    @Deprecated
    void waitForAutoRun(User authUser, int maximumTime, ImagingSession session) {
        interfaceFor(authUser).waitForAutoRun(session, maximumTime)
    }

    void waitForAutoRun(ImagingSession session) {
        waitForAutoRun(mainUser, 60, session)
    }

    String getBuildInfo() {
        try {
            return mainInterface().buildInfo
        } catch (JsonPathException ignored) {
            log.info('Couldn\'t get build information because of JsonPathException (likely means site has not been initialized).')
        } catch (Exception ignored) {
            log.warn('Couldn\'t get build information because of: ', ignored)
        }
        null
    }

    void clearPrearchiveSessions(User authUser, Project project) {
        clearPrearchiveSessionsMatchingFilter(authUser, formatRestUrl("/prearchive/projects/${project.id}"), 'ResultSet.Result.collect { it.url }')
    }

    void clearUnassignedPrearchiveSessions(User authUser, List<String> studyInstanceUIDs) {
        clearPrearchiveSessionsMatchingFilter(authUser, formatRestUrl('/prearchive'), "ResultSet.Result.findAll { it.tag in ${studyInstanceUIDs.collect { "'${it}'" }} && it.project == 'Unassigned' }.url")
    }

    void waitForPrearchiveEmpty(User authUser, Project project, int maximumWait) {
        final StopWatch stopWatch = TimeUtils.launchStopWatch()
        while (true) {
            TimeUtils.checkStopWatch(stopWatch, maximumWait, "Prearchive did not empty for project ${project}")
            if (interfaceFor(authUser).jsonQuery().get(formatRestUrl("/prearchive/projects/${project.id}")).jsonPath().getInt('ResultSet.Result.size()') == 0) {
                return
            } else {
                TimeUtils.sleep(1000)
            }
        }
    }

    @Deprecated
    void clearProject(User authUser, Project project) {
        interfaceFor(authUser).deleteAllProjectData(project)
    }

    void uploadToSessionZipImporter(User authUser, File sessionZip, Project project, Subject subject, ImagingSession session) {
        interfaceFor(authUser ?: mainUser).uploadToSessionZipImporter(sessionZip, project, subject, session)
    }

    void uploadToSessionZipImporter(File sessionZip, Project project) {
        uploadToSessionZipImporter(null, sessionZip, project, null, null)
    }

    void uploadToSessionZipImporter(TestData testData, Project project) {
        uploadToSessionZipImporter(testData.toFile(), project)
    }

    void uploadToSessionZipImporter(File sessionZip, ImagingSession session) {
        if (session.primaryProject == null) {
            throw new IllegalArgumentException('Session must have project object specified to use this shortcut method')
        }
        uploadToSessionZipImporter(null, sessionZip, session.primaryProject, session.subject, session)
    }

    String getUserSessionsRestUrl(User user) {
        mainInterface().userSessionsRestUrl(user)
    }

    @Deprecated
    void expireAllActiveSessions(User authUser, User targetUser) {
        interfaceFor(authUser).expireAllActiveSessions(targetUser)
    }

    @Deprecated
    int getNumberActiveSessions(User user) {
        interfaceFor(user).numberActiveSessions
    }

    @Deprecated
    User readUser(User authUser, String username) {
        interfaceFor(authUser).readUser(username)
    }

    void createUser(User targetUser) {
        createUser(adminUser, targetUser)
    }

    @Deprecated
    void createUser(User authUser, User user) {
        interfaceFor(authUser).createUser(user)
    }

    @Deprecated
    void updateUser(User authUser, User targetUser) {
        interfaceFor(authUser).updateUser(targetUser)
    }

    @Deprecated
    void assignUserToRoles(User authUser, User targetUser, String... roles) {
        interfaceFor(authUser).assignUserToRoles(targetUser, roles)
    }

    @Deprecated
    void addUserToGroups(User authUser, User targetUser, String... groups) {
        interfaceFor(authUser).addUserToGroups(targetUser, groups)
    }

    @Deprecated
    void removeUserFromGroups(User authUser, User targetUser, String... groups) {
        interfaceFor(authUser).removeUserFromGroups(targetUser, groups)
    }

    @Deprecated
    void verifyUser(User authUser, User targetUser) {
        interfaceFor(authUser).verifyUser(targetUser)
    }

    @Deprecated
    void enableUser(User authUser, User targetUser) {
        interfaceFor(authUser).enableUser(targetUser)
    }

    @Deprecated
    void makeUserAdmin(User authUser, User targetUser) {
        interfaceFor(authUser).makeUserAdmin(targetUser)
    }

    @Deprecated
    void postToSiteConfig(User authUser, Map configSettings) {
        interfaceFor(authUser).postToSiteConfig(configSettings)
    }

    @Deprecated
    void setLoginRequirement(User authUser, boolean loginRequired) {
        interfaceFor(authUser).setLoginRequirement(loginRequired)
    }

    @Deprecated
    void openXnat(User authUser) {
        interfaceFor(authUser).openXnat()
    }

    @Deprecated
    void closeXnat(User authUser) {
        interfaceFor(authUser).closeXnat()
    }

    void setAutoArchiveTimings(User authUser, int idleTime, int idleSchedule) {
        postToSiteConfig(authUser, [
                (SiteConfig.AUTOARCHIVE_IDLE_TIME) : idleTime,
                (SiteConfig.AUTOARCHIVE_IDLE_SCHEDULE) : idleSchedule
        ])
    }

    void setDicomRoutingConfig(RoutingRulesType routingType, String contents) {
        interfaceFor(mainAdminUser).setDicomRoutingConfig(routingType, contents)
    }

    void disableDicomRoutingConfig(RoutingRulesType routingType) {
        interfaceFor(mainAdminUser).disableDicomRoutingConfig(routingType)
    }

    void setProjectDicomRoutingConfig(String contents) {
        setDicomRoutingConfig(RoutingRulesType.PROJECT_RULES, contents)
    }

    void setSubjectDicomRoutingConfig(String contents) {
        setDicomRoutingConfig(RoutingRulesType.SUBJECT_RULES, contents)
    }

    void setSessionDicomRoutingConfig(String contents) {
        setDicomRoutingConfig(RoutingRulesType.SESSION_RULES, contents)
    }

    void disableProjectDicomRoutingConfig() {
        disableDicomRoutingConfig(RoutingRulesType.PROJECT_RULES)
    }

    void disableSubjectDicomRoutingConfig() {
        disableDicomRoutingConfig(RoutingRulesType.SUBJECT_RULES)
    }

    void disableSessionDicomRoutingConfig() {
        disableDicomRoutingConfig(RoutingRulesType.SESSION_RULES)
    }

    @Deprecated
    void setDicomProjectRules(User authUser, String ruleString) {
        interfaceFor(authUser).setDicomProjectRules(ruleString)
    }

    @Deprecated
    void setDicomProjectRulesFrom(User authUser, int dicomElement, String regex) {
        interfaceFor(authUser).setDicomProjectRulesFrom(dicomElement, regex)
    }

    String siteAnonScriptUrl() {
        mainInterface().legacySiteAnonScriptUrl()
    }

    @Deprecated
    AnonScript getSiteAnonScript(User authUser) {
        interfaceFor(authUser).readSiteAnonScript()
    }

    @Deprecated
    void setSiteAnonScriptStatus(User authUser, boolean status) {
        interfaceFor(authUser).setSiteAnonScriptStatus(status)
    }

    @Deprecated
    void setSiteAnonScript(User authUser, AnonScript script) {
        interfaceFor(authUser).setSiteAnonScript(script)
    }

    @Deprecated
    void disableSiteAnonScript(User authUser) {
        setSiteAnonScriptStatus(authUser, false)
    }

    @Deprecated
    void enableSiteAnonScript(User authUser) {
        setSiteAnonScriptStatus(authUser, true)
    }

    String projectAnonScriptUrl(Project project) {
        mainInterface().projectAnonScriptUrl(project)
    }

    @Deprecated
    AnonScript getProjectAnonScript(User authUser, Project project) {
        interfaceFor(authUser).readProjectAnonScript(project)
    }

    @Deprecated
    void setProjectAnonScript(User authUser, Project project, AnonScript script) {
        interfaceFor(authUser).setProjectAnonScript(project, script)
    }

    @Deprecated
    void setProjectAnonScriptStatus(User authUser, Project project, boolean status) {
        interfaceFor(authUser).setProjectAnonScriptStatus(project, status)
    }

    @Deprecated
    void disableProjectAnonScript(User authUser, Project project) {
        setProjectAnonScriptStatus(authUser, project, false)
    }

    @Deprecated
    void enableProjectAnonScript(User authUser, Project project) {
        setProjectAnonScriptStatus(authUser, project, true)
    }

    @Deprecated
    List<Investigator> readInvestigators(User authUser) {
        interfaceFor(authUser).readInvestigators()
    }

    @Deprecated
    void createInvestigators(User authUser, List<Investigator> investigators) {
        interfaceFor(authUser).createInvestigators(investigators)
    }

    @Deprecated
    void createInvestigator(User authUser, Investigator investigator) {
        interfaceFor(authUser).createInvestigator(investigator)
    }

    @Deprecated
    void addUserToProject(User authUser, User addedUser, Project project, UserGroup userGroup) {
        interfaceFor(authUser).addUserToProject(addedUser, project, userGroup)
    }

    @Deprecated
    void uploadResources(User authUser, List<Resource> resources) {
        resources.each { resource ->
            uploadResource(authUser, resource)
        }
    }

    @Deprecated
    void uploadResource(User authUser, Resource resource) {
        interfaceFor(authUser).uploadResource(resource)
    }

    @Deprecated
    void deleteResource(User authUser, Resource resource) {
        interfaceFor(authUser).deleteResource(resource)
    }

    String resourceFilesUrl(Resource resource) {
        mainInterface().resourceFilesUrl(resource)
    }

    String resourceFileUrl(Resource resource, ResourceFile file) {
        mainInterface().resourceFileUrl(resource, file)
    }

    void validateUpload(User authUser, String fileUrl, File localFile) {
        TestNgUtils.assertBinaryFilesEqual(
                localFile,
                saveBinaryResponseToFile(Credentials.build(authUser).get(fileUrl)))
    }

    void validateResource(User authUser, Resource resource) {
        resource.resourceFiles.each { resourceFile ->
            validateUpload(authUser, resourceFileUrl(resource, resourceFile), resourceFile.extension.javaFile)
        }
    }

    @Deprecated
    Project readProject(User authUser, String projectId) {
        interfaceFor(authUser).readProject(projectId)
    }

    @Deprecated
    List<Subject> readSubjects(User authUser, Project project) {
        interfaceFor(authUser).readSubjects(project)
    }

    @Deprecated
    List<SubjectAssessor> readSubjectAssesssors(User authUser, Project project, Subject subject) {
        interfaceFor(authUser).readSubjectAssessors(project, subject)
    }

    @Deprecated
    List<Scan> readScans(User authUser, Project project, Subject subject, ImagingSession session) {
        interfaceFor(authUser).readScans(project, subject, session)
    }

    @Deprecated
    List<SessionAssessor> readSessionAssessors(User authUser, Project project, Subject subject, ImagingSession session) {
        interfaceFor(authUser).readSessionAssessors(project, subject, session)
    }

    @Deprecated
    void populateAdditionalScanMetadata(User authUser, Project project, Subject subject, ImagingSession session) {
        interfaceFor(authUser).populateAdditionalScanMetadata(project, subject, session)
    }

    @Deprecated
    Scan readAdditionalScanMetadata(User authUser, Project project, Subject subject, ImagingSession session, Scan scan) {
        interfaceFor(authUser).readAdditionalScanMetadata(project, subject, session, scan)
    }

    Resource findResource(List<Resource> resources, String label) {
        mainInterface().findResource(resources, label)
    }

    String accessibilityRestUrl(Project project) {
        mainInterface().accessibilityRestUrl(project)
    }

    String accessibilityRestUrl(Project project, Accessibility accessibility) {
        mainInterface().accessibilityRestUrl(project, accessibility)
    }

    @Deprecated
    void updateAccessibility(User authUser, Project project, Accessibility accessibility) {
        interfaceFor(authUser).updateAccessibility(project, accessibility)
    }

    void assertProjectAccessibility(User authUser, Project project, Accessibility accessibility) {
        interfaceFor(authUser).xmlQuery().get(accessibilityRestUrl(project)).then().assertThat().statusCode(200).and().body(equalTo(accessibility.toString()))
    }

    String projectUrl(Project project) {
        mainInterface().projectUrl(project)
    }

    void createProject(User authUser, Project project, File projectXmlFile) {
        new ProjectXMLPutExtension(interfaceFor(authUser), project, projectXmlFile).create()
    }

    @Deprecated
    void createProject(User authUser, Project project) {
        interfaceFor(authUser).createProject(project)
    }

    @Deprecated
    void setPrearchiveSetting(User authUser, Project project, PrearchiveCode code) {
        interfaceFor(authUser).setPrearchiveSetting(project, code)
    }

    @Deprecated
    void createSubject(User authUser, Project project, Subject subject) {
        interfaceFor(authUser).createSubject(project, subject)
    }

    @Deprecated
    void createSubject(User authUser, Subject subject) {
        interfaceFor(authUser).createSubject(subject)
    }

    Subject createSubject(User authUser, Project project, File subjectXML) {
        final SubjectExtension extension = new SubjectXMLPutExtension(interfaceFor(authUser), subjectXML)
        extension.create(project)
        extension.parentObject
    }

    @Deprecated
    void relabelSubject(User authUser, Project project, Subject subject, String newLabel) {
        interfaceFor(authUser).relabelSubject(project, subject, newLabel)
    }

    @Deprecated
    void relabelSubject(User authUser, Subject subject, String newLabel) {
        interfaceFor(authUser).relabelSubject(subject, newLabel)
    }

    @Deprecated
    void shareSubject(User authUser, Project sourceProject, Subject subject, Share share) {
        interfaceFor(authUser).shareSubject(sourceProject, subject, share)
    }

    @Deprecated
    void deleteSubject(User authUser, Project project, Subject subject) {
        interfaceFor(authUser).deleteSubject(project, subject)
    }

    @Deprecated
    void deleteSubject(User authUser, Subject subject) {
        interfaceFor(authUser).deleteSubject(subject)
    }

    String subjectUrl(Project project, Subject subject) {
        mainInterface().subjectUrl(project, subject)
    }

    String subjectUrl(Subject subject) {
        mainInterface().subjectUrl(subject)
    }

    @Deprecated
    void createSubjectAssessor(User authUser, Project project, Subject subject, SubjectAssessor subjectAssessor) {
        interfaceFor(authUser).createSubjectAssessor(project, subject, subjectAssessor)
    }

    @Deprecated
    void createSubjectAssessor(User authUser, SubjectAssessor subjectAssessor) {
        interfaceFor(authUser).createSubjectAssessor(subjectAssessor)
    }

    @Deprecated
    void relabelSubjectAssessor(User authUser, Project project, Subject subject, SubjectAssessor subjectAssessor, String newLabel) {
        interfaceFor(authUser).relabelSubjectAssessor(project, subject, subjectAssessor, newLabel)
    }

    @Deprecated
    void relabelSubjectAssessor(User authUser, SubjectAssessor subjectAssessor, String newLabel) {
        interfaceFor(authUser).relabelSubjectAssessor(subjectAssessor, newLabel)
    }

    @Deprecated
    void shareSubjectAssessor(User authUser, SubjectAssessor subjectAssessor, Share share) {
        interfaceFor(authUser).shareSubjectAssessor(subjectAssessor, share)
    }

    @Deprecated
    void deleteSubjectAssessor(User authUser, Project project, Subject subject, SubjectAssessor subjectAssessor) {
        interfaceFor(authUser).deleteSubjectAssessor(project, subject, subjectAssessor)
    }

    @Deprecated
    void deleteSubjectAssessor(User authUser, SubjectAssessor subjectAssessor) {
        interfaceFor(authUser).deleteSubjectAssessor(subjectAssessor)
    }

    String subjectAssessorUrl(Project project, Subject subject, SubjectAssessor assessor) {
        mainInterface().subjectAssessorUrl(project, subject, assessor)
    }

    String subjectAssessorUrl(SubjectAssessor assessor) {
        mainInterface().subjectAssessorUrl(assessor)
    }

    String sessionScansUrl(Project project, Subject subject, ImagingSession session) {
        mainInterface().sessionScansUrl(project, subject, session)
    }

    String sessionScansUrl(ImagingSession session) {
        mainInterface().sessionScansUrl(session)
    }

    @Deprecated
    void createScan(User authUser, Project project, Subject subject, ImagingSession session, Scan scan) {
        interfaceFor(authUser).createScan(project, subject, session, scan)
    }

    @Deprecated
    void deleteScan(User authUser, Project project, Subject subject, ImagingSession session, Scan scan) {
        interfaceFor(authUser).deleteScan(project, subject, session, scan)
    }

    @Deprecated
    void deleteScan(User authUser, Scan scan) {
        interfaceFor(authUser).deleteScan(scan)
    }

    @Deprecated
    void updateScan(User authUser, Project project, Subject subject, ImagingSession session, Scan scan) {
        interfaceFor(authUser).updateScan(project, subject, session, scan)
    }

    @Deprecated
    void updateScan(User authUser, Scan scan) {
        interfaceFor(authUser).updateScan(scan)
    }

    String scanUrl(Project project, Subject subject, ImagingSession session, Scan scan) {
        mainInterface().scanUrl(project, subject, session, scan)
    }

    String scanUrl(Scan scan) {
        mainInterface().scanUrl(scan)
    }

    String assessorsUrl(Project project, Subject subject, ImagingSession session) {
        mainInterface().assessorsUrl(project, subject, session)
    }

    @Deprecated
    void createSessionAssessor(User authUser, Project project, Subject subject, ImagingSession session, SessionAssessor assessor) {
        interfaceFor(authUser).createSessionAssessor(project, subject, session, assessor)
    }

    @Deprecated
    void createSessionAssessor(User authUser, SessionAssessor assessor) {
        interfaceFor(authUser).createSessionAssessor(assessor)
    }

    @Deprecated
    void deleteSessionAssessor(User authUser, Project project, Subject subject, ImagingSession session, SessionAssessor sessionAssessor) {
        interfaceFor(authUser).deleteSessionAssessor(project, subject, session, sessionAssessor)
    }

    @Deprecated
    void deleteSessionAssessor(User authUser, SessionAssessor sessionAssessor) {
        interfaceFor(authUser).deleteSessionAssessor(sessionAssessor)
    }

    String sessionAssessorUrl(Project project, Subject subject, ImagingSession session, SessionAssessor sessionAssessor) {
        mainInterface().sessionAssessorUrl(project, subject, session, sessionAssessor)
    }

    String sessionAssessorUrl(SessionAssessor assessor) {
        mainInterface().sessionAssessorUrl(assessor)
    }

    String reconstructionUrl(Project project, Subject subject, ImagingSession session, Reconstruction reconstruction) {
        mainInterface().reconstructionUrl(project, subject, session, reconstruction)
    }

    String reconstructionUrl(Reconstruction reconstruction) {
        mainInterface().reconstructionUrl(reconstruction)
    }

    @Deprecated
    void deleteProject(User authUser, Project project) {
        interfaceFor(authUser).deleteProject(project)
    }

    void deleteProjectSilently(User authUser, Project project) {
        try {
            deleteProject(authUser, project)
        } catch (Exception | Error ignored) {}
    }

    String assessorsByAccessionNumber(ImagingSession session) {
        mainInterface().assessorsUrlByAccessionNumber(session)
    }

    String assessorByAccessionNumber(ImagingSession session, SessionAssessor assessor) {
        mainInterface().assessorUrlByAccessionNumber(session, assessor)
    }

    void initializeXnat() {
        final Response initResponse = adminCredentials.get(formatXapiUrl('/siteConfig/initialized'))
        if (initResponse.statusCode() == 200 && initResponse.as(Boolean)) {
            log.info('XNAT already initialized')
        } else {
            interfaceFor(adminUser).queryBase().contentType(JSON).body(['initialized' : true]).post(formatXapiUrl('siteConfig')).then().assertThat().statusCode(200)
        }
    }

    void setupTestUsers() {
        final List<String> allUsers = adminCredentials.get(formatXapiUrl('/users')).path('')

        if (mainUser.username in allUsers) {
            verifyUser(adminUser, mainUser)
            enableUser(adminUser, mainUser)
        } else {
            createUser(xnatConfig.mainUser.email(Settings.EMAIL))
        }

        if (mainAdminUser.username in allUsers) {
            verifyUser(adminUser, mainAdminUser)
            enableUser(adminUser, mainAdminUser)
            makeUserAdmin(adminUser, mainAdminUser)
        } else {
            createUser(mainAdminUser.email(Settings.EMAIL))
            makeUserAdmin(adminUser, mainAdminUser)
        }
    }

    private void clearPrearchiveSessionsMatchingFilter(User authUser, String prearchiveQueryUrl, String jsonPathFilter) {
        interfaceFor(authUser).jsonQuery().get(prearchiveQueryUrl).then().assertThat().statusCode(200).and().extract().jsonPath().getList(jsonPathFilter).each { deleteUrl ->
            queryBaseFor(authUser).delete(formatRestUrl(deleteUrl as String)).then().assertThat().statusCode(200)
        }
    }

}
