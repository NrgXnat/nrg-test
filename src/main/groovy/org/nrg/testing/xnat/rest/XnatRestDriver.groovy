package org.nrg.testing.xnat.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.restassured.internal.RestAssuredResponseImpl
import com.jayway.restassured.path.json.exception.JsonPathException
import com.jayway.restassured.response.Response
import com.jayway.restassured.specification.RequestSender
import com.jayway.restassured.specification.RequestSpecification
import groovy.util.logging.Log4j
import org.apache.commons.lang3.time.StopWatch
import org.apache.http.entity.ContentType
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
import org.nrg.testing.xnat.versions.XnatVersion
import org.nrg.testing.xnat.versions.XnatVersionList
import org.nrg.xnat.enums.Accessibility
import org.nrg.xnat.enums.DicomEditVersion
import org.nrg.xnat.enums.PrearchiveCode
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

import java.nio.file.Paths

import static org.hamcrest.CoreMatchers.equalTo

@SuppressWarnings('unused')
@Log4j
abstract class XnatRestDriver {

    XnatConfig xnatConfig
    TestController testController
    protected static final Map<User, XnatInterface> xnatInterfaceMap = [:]
    public static final ObjectMapper XNAT_REST_MAPPER = XnatInterface.XNAT_REST_MAPPER

    abstract List<Class<? extends XnatVersion>> getHandledVersions()

    static XnatRestDriver getInstance(XnatConfig xnatConfig) {
        final XnatConfig config = xnatConfig ?: Settings.DEFAULT_XNAT_CONFIG
        final XnatRestDriver restDriver = XnatVersionList.lookupDriverClass(config.xnatVersion).newInstance()
        restDriver.setXnatConfig(config)
        restDriver
    }

    static XnatRestDriver getInstance() {
        getInstance(Settings.DEFAULT_XNAT_CONFIG)
    }

    XnatInterface interfaceFor(User user) {
        final XnatInterface xnatInterface = xnatInterfaceMap[user]
        if (xnatInterface != null) {
            xnatInterface
        } else {
            final XnatInterface newInterface = XnatInterface.authenticate(xnatConfig.xnatUrl, user, true)
            xnatInterfaceMap.put(user, newInterface)
            newInterface
        }
    }

    XnatInterface mainInterface() {
        interfaceFor(mainUser())
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
        fixContentType(response, ContentType.APPLICATION_JSON)
        response
    }

    void fixContentType(Response restResponse, ContentType type) {
        (restResponse as RestAssuredResponseImpl).setContentType(type.mimeType) // XNAT is returning the wrong content type in some cases
    }

    File saveBinaryResponseToFile(Response response) {
        final File downloadedFile = Paths.get(Settings.TEMP_SUBDIR, "${RandomHelper.randomID()}.binarytestfile").toFile()
        HttpUtils.saveBinaryResponseToFile(response, downloadedFile)
        downloadedFile
    }

    RequestSpecification mainCredentials() {
        xnatConfig.mainCredentials
    }

    RequestSpecification mainAdminCredentials() {
        xnatConfig.mainAdminCredentials
    }

    RequestSpecification adminCredentials() {
        xnatConfig.adminCredentials
    }

    RequestSpecification invalidCredentials() {
        Credentials.build(RandomHelper.randomLetters(12), RandomHelper.randomLetters(12)) // randomly generating this is fine. Probability of collision is astronomically small with 12 letters
    }

    User mainUser() {
        xnatConfig.mainUser
    }

    User mainAdminUser() {
        xnatConfig.mainAdminUser
    }

    User adminUser() {
        xnatConfig.adminUser
    }

    String aliasTokenUrl() {
        mainInterface().issueAliasTokenUrl()
    }

    XnatAliasToken generateAliasToken(User user) {
        interfaceFor(user).generateAliasToken()
    }

    List<XnatPlugin> readInstalledPlugins(User user) {
        interfaceFor(user).readInstalledPlugins()
    }

    AnonScript getDefaultXnatAnonScript() {
        XnatObjectUtils.anonScriptFromURL(DicomEditVersion.UNSPECIFIED, formatXapiUrl('anonymize/default'), Settings.DEFAULT_XNAT_CONFIG.adminUser)
    }
    
    String projectExperimentsUrl(Project project) {
        mainInterface().projectExperimentsUrl(project)
    }

    String getAccessionNumber(User authUser, SubjectAssessor subjectAssessor) {
        interfaceFor(authUser).getAccessionNumber(subjectAssessor)
    }

    String getAccessionNumber(ImagingSession session) {
        getAccessionNumber(mainUser(), session)
    }

    Subject readSubject(User authUser, String accessionNumber) {
        interfaceFor(authUser).readSubject(accessionNumber)
    }

    def <T extends Experiment> T readExperiment(User authUser, String accessionNumber, Class<T> tClass) {
        interfaceFor(authUser).readExperiment(accessionNumber, tClass)
    }

    void waitForAutoRun(User authUser, int maximumTime, ImagingSession session) {
        interfaceFor(authUser).waitForAutoRun(session, maximumTime)
    }

    void waitForAutoRun(ImagingSession session) {
        waitForAutoRun(mainUser(), 60, session)
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
        clearPrearchiveSessionsMatchingFilter(authUser, formatRestUrl('/prearchive'), "ResultSet.Result.findAll { it.tag in ${studyInstanceUIDs} && it.project == 'Unassigned' }.url")
    }

    void waitForPrearchiveEmpty(User authUser, Project project, int maximumWait) {
        final StopWatch stopWatch = TimeUtils.launchStopWatch()
        while (true) {
            TimeUtils.checkStopWatch(stopWatch, maximumWait, "Prearchive did not empty for project ${project}")
            if (interfaceFor(authUser).jsonQuery().get(formatRestUrl("/prearchive/projects/${project.id}")).jsonPath().getInt('ResultSet.Result.size()') == 0){
                return
            } else {
                TimeUtils.sleep(1000)
            }
        }
    }

    void clearProject(User authUser, Project project) {
        interfaceFor(authUser).deleteAllProjectData(project)
    }

    void uploadToSessionZipImporter(User authUser, File sessionZip, Project project, Subject subject, ImagingSession session) {
        interfaceFor(authUser ?: mainUser()).uploadToSessionZipImporter(sessionZip, project, subject, session)
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

    void expireAllActiveSessions(User authUser, User targetUser) {
        interfaceFor(authUser).expireAllActiveSessions(targetUser)
    }

    int getNumberActiveSessions(User user) {
        interfaceFor(user).numberActiveSessions
    }

    User readUser(User authUser, String username) {
        interfaceFor(authUser).readUser(username)
    }

    void createUser(User targetUser) {
        createUser(adminUser(), targetUser)
    }

    void createUser(User authUser, User user) {
        interfaceFor(authUser).createUser(user)
    }

    void updateUser(User authUser, User targetUser) {
        interfaceFor(authUser).updateUser(targetUser)
    }

    void assignUserToRoles(User authUser, User targetUser, String... roles) {
        interfaceFor(authUser).assignUserToRoles(targetUser, roles)
    }

    void addUserToGroups(User authUser, User targetUser, String... groups) {
        interfaceFor(authUser).addUserToGroups(targetUser, groups)
    }

    void removeUserFromGroups(User authUser, User targetUser, String... groups) {
        interfaceFor(authUser).removeUserFromGroups(targetUser, groups)
    }

    void verifyUser(User authUser, User targetUser) {
        interfaceFor(authUser).verifyUser(targetUser)
    }

    void enableUser(User authUser, User targetUser) {
        interfaceFor(authUser).enableUser(targetUser)
    }

    void makeUserAdmin(User authUser, User targetUser) {
        interfaceFor(authUser).makeUserAdmin(targetUser)
    }

    void postToSiteConfig(User authUser, Map configSettings) {
        interfaceFor(authUser).postToSiteConfig(configSettings)
    }

    void setLoginRequirement(User authUser, boolean loginRequired) {
        interfaceFor(authUser).setLoginRequirement(loginRequired)
    }

    void openXnat(User authUser) {
        interfaceFor(authUser).openXnat()
    }

    void closeXnat(User authUser) {
        interfaceFor(authUser).closeXnat()
    }

    void setAutoArchiveTimings(User authUser, int idleTime, int idleSchedule) {
        postToSiteConfig(authUser, [
                (SiteConfig.AUTOARCHIVE_IDLE_TIME) : idleTime,
                (SiteConfig.AUTOARCHIVE_IDLE_SCHEDULE) : idleSchedule
        ])
    }

    String siteAnonScriptUrl() {
        mainInterface().legacySiteAnonScriptUrl()
    }

    AnonScript getSiteAnonScript(User authUser) {
        interfaceFor(authUser).readSiteAnonScript()
    }

    void setSiteAnonScriptStatus(User authUser, boolean status) {
        interfaceFor(authUser).setSiteAnonScriptStatus(status)
    }

    void setSiteAnonScript(User authUser, AnonScript script) {
        interfaceFor(authUser).setSiteAnonScript(script)
    }

    void disableSiteAnonScript(User authUser) {
        setSiteAnonScriptStatus(authUser, false)
    }

    void enableSiteAnonScript(User authUser) {
        setSiteAnonScriptStatus(authUser, true)
    }

    String projectAnonScriptUrl(Project project) {
        mainInterface().projectAnonScriptUrl(project)
    }

    AnonScript getProjectAnonScript(User authUser, Project project) {
        interfaceFor(authUser).readProjectAnonScript(project)
    }

    void setProjectAnonScript(User authUser, Project project, AnonScript script) {
        interfaceFor(authUser).setProjectAnonScript(project, script)
    }

    void setProjectAnonScriptStatus(User authUser, Project project, boolean status) {
        interfaceFor(authUser).setProjectAnonScriptStatus(project, status)
    }

    void disableProjectAnonScript(User authUser, Project project) {
        setProjectAnonScriptStatus(authUser, project, false)
    }

    void enableProjectAnonScript(User authUser, Project project) {
        setProjectAnonScriptStatus(authUser, project, true)
    }

    List<Investigator> readInvestigators(User authUser) {
        interfaceFor(authUser).readInvestigators()
    }

    void createInvestigators(User authUser, List<Investigator> investigators) {
        interfaceFor(authUser).createInvestigators(investigators)
    }

    void createInvestigator(User authUser, Investigator investigator) {
        interfaceFor(authUser).createInvestigator(investigator)
    }

    void addUserToProject(User authUser, User addedUser, Project project, UserGroup userGroup) {
        interfaceFor(authUser).addUserToProject(addedUser, project, userGroup)
    }

    void uploadResources(User authUser, List<Resource> resources) {
        resources.each { resource ->
            uploadResource(authUser, resource)
        }
    }

    void uploadResource(User authUser, Resource resource) {
        interfaceFor(authUser).uploadResource(resource)
    }

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

    Project readProject(User authUser, String projectId) {
        interfaceFor(authUser).readProject(projectId)
    }

    List<Subject> readSubjects(User authUser, Project project) {
        interfaceFor(authUser).readSubjects(project)
    }

    List<SubjectAssessor> readSubjectAssesssors(User authUser, Project project, Subject subject) {
        interfaceFor(authUser).readSubjectAssessors(project, subject)
    }

    List<Scan> readScans(User authUser, Project project, Subject subject, ImagingSession session) {
        interfaceFor(authUser).readScans(project, subject, session)
    }

    List<SessionAssessor> readSessionAssessors(User authUser, Project project, Subject subject, ImagingSession session) {
        interfaceFor(authUser).readSessionAssessors(project, subject, session)
    }

    void populateAdditionalScanMetadata(User authUser, Project project, Subject subject, ImagingSession session) {
        interfaceFor(authUser).populateAdditionalScanMetadata(project, subject, session)
    }

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

    void createProject(User authUser, Project project) {
        interfaceFor(authUser).createProject(project)
    }

    void setPrearchiveSetting(User authUser, Project project, PrearchiveCode code) {
        interfaceFor(authUser).setPrearchiveSetting(project, code)
    }

    void createSubject(User authUser, Project project, Subject subject) {
        interfaceFor(authUser).createSubject(project, subject)
    }

    void createSubject(User authUser, Subject subject) {
        interfaceFor(authUser).createSubject(subject)
    }

    Subject createSubject(User authUser, Project project, File subjectXML) {
        final SubjectExtension extension = new SubjectXMLPutExtension(interfaceFor(authUser), subjectXML)
        extension.create(project)
        extension.parentObject
    }

    void relabelSubject(User authUser, Project project, Subject subject, String newLabel) {
        interfaceFor(authUser).relabelSubject(project, subject, newLabel)
    }

    void relabelSubject(User authUser, Subject subject, String newLabel) {
        interfaceFor(authUser).relabelSubject(subject, newLabel)
    }

    void shareSubject(User authUser, Project sourceProject, Subject subject, Share share) {
        interfaceFor(authUser).shareSubject(sourceProject, subject, share)
    }

    void deleteSubject(User authUser, Project project, Subject subject) {
        interfaceFor(authUser).deleteSubject(project, subject)
    }

    void deleteSubject(User authUser, Subject subject) {
        interfaceFor(authUser).deleteSubject(subject)
    }

    String subjectUrl(Project project, Subject subject) {
        mainInterface().subjectUrl(project, subject)
    }

    String subjectUrl(Subject subject) {
        mainInterface().subjectUrl(subject)
    }

    void createSubjectAssessor(User authUser, Project project, Subject subject, SubjectAssessor subjectAssessor) {
        interfaceFor(authUser).createSubjectAssessor(project, subject, subjectAssessor)
    }

    void createSubjectAssessor(User authUser, SubjectAssessor subjectAssessor) {
        interfaceFor(authUser).createSubjectAssessor(subjectAssessor)
    }

    void relabelSubjectAssessor(User authUser, Project project, Subject subject, SubjectAssessor subjectAssessor, String newLabel) {
        interfaceFor(authUser).relabelSubjectAssessor(project, subject, subjectAssessor, newLabel)
    }

    void relabelSubjectAssessor(User authUser, SubjectAssessor subjectAssessor, String newLabel) {
        interfaceFor(authUser).relabelSubjectAssessor(subjectAssessor, newLabel)
    }

    void shareSubjectAssessor(User authUser, SubjectAssessor subjectAssessor, Share share) {
        interfaceFor(authUser).shareSubjectAssessor(subjectAssessor, share)
    }

    void deleteSubjectAssessor(User authUser, Project project, Subject subject, SubjectAssessor subjectAssessor) {
        interfaceFor(authUser).deleteSubjectAssessor(project, subject, subjectAssessor)
    }

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

    void createScan(User authUser, Project project, Subject subject, ImagingSession session, Scan scan) {
        interfaceFor(authUser).createScan(project, subject, session, scan)
    }

    void deleteScan(User authUser, Project project, Subject subject, ImagingSession session, Scan scan) {
        interfaceFor(authUser).deleteScan(project, subject, session, scan)
    }

    void deleteScan(User authUser, Scan scan) {
        interfaceFor(authUser).deleteScan(scan)
    }

    void updateScan(User authUser, Project project, Subject subject, ImagingSession session, Scan scan) {
        interfaceFor(authUser).updateScan(project, subject, session, scan)
    }

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

    void createSessionAssessor(User authUser, Project project, Subject subject, ImagingSession session, SessionAssessor assessor) {
        interfaceFor(authUser).createSessionAssessor(project, subject, session, assessor)
    }

    void createSessionAssessor(User authUser, SessionAssessor assessor) {
        interfaceFor(authUser).createSessionAssessor(assessor)
    }

    void deleteSessionAssessor(User authUser, Project project, Subject subject, ImagingSession session, SessionAssessor sessionAssessor) {
        interfaceFor(authUser).deleteSessionAssessor(project, subject, session, sessionAssessor)
    }

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
        final Response initResponse = adminCredentials().get(formatXapiUrl('/siteConfig/initialized'))
        if (initResponse.statusCode() == 200 && initResponse.as(Boolean)) {
            log.info('XNAT already initialized')
        } else {
            postToSiteConfig(adminUser(), ['initialized' : true])
        }
    }

    void setupTestUsers() {
        final List<String> allUsers = adminCredentials().get(formatXapiUrl('/users')).path('')

        if (mainUser().username in allUsers) {
            verifyUser(adminUser(), mainUser())
            enableUser(adminUser(), mainUser())
        } else {
            createUser(xnatConfig.mainUser.email(Settings.EMAIL))
        }

        if (mainAdminUser().username in allUsers) {
            verifyUser(adminUser(), mainAdminUser())
            enableUser(adminUser(), mainAdminUser())
            makeUserAdmin(adminUser(), mainAdminUser())
        } else {
            createUser(mainAdminUser().email(Settings.EMAIL))
            makeUserAdmin(adminUser(), mainAdminUser())
        }
    }

    private void clearPrearchiveSessionsMatchingFilter(User authUser, String prearchiveQueryUrl, String jsonPathFilter) {
        interfaceFor(authUser).jsonQuery().get(prearchiveQueryUrl).then().assertThat().statusCode(200).and().extract().jsonPath().getList(jsonPathFilter).each { deleteUrl ->
            queryBaseFor(authUser).delete(formatRestUrl(deleteUrl as String)).then().assertThat().statusCode(200)
        }
    }

}
