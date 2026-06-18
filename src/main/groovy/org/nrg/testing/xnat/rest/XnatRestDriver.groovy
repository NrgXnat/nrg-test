package org.nrg.testing.xnat.rest

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.util.logging.Log4j
import io.restassured.http.ContentType
import io.restassured.internal.RestAssuredResponseImpl
import io.restassured.path.json.exception.JsonPathException
import io.restassured.response.Response
import io.restassured.specification.RequestSender
import io.restassured.specification.RequestSpecification
import org.apache.commons.lang3.time.StopWatch
import org.nrg.jira.components.zephyr.TestStatus
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
import org.nrg.xnat.XnatConnectionConfig
import org.nrg.xnat.enums.Accessibility
import org.nrg.xnat.enums.DicomEditVersion
import org.nrg.xnat.interfaces.XnatInterface
import org.nrg.xnat.pogo.*
import org.nrg.xnat.pogo.experiments.*
import org.nrg.xnat.pogo.extensions.project.ProjectXMLPutExtension
import org.nrg.xnat.pogo.extensions.subject.SubjectExtension
import org.nrg.xnat.pogo.extensions.subject.SubjectXMLPutExtension
import org.nrg.xnat.pogo.resources.Resource
import org.nrg.xnat.pogo.users.User
import org.nrg.xnat.prearchive.SessionData
import org.nrg.xnat.rest.Credentials
import org.nrg.xnat.versions.XnatVersion

import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.regex.Matcher
import java.util.regex.Pattern

import static org.hamcrest.CoreMatchers.equalTo
import static org.testng.AssertJUnit.assertTrue
import static org.testng.AssertJUnit.fail

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
    protected static final Pattern SERVER_TIME_REGEX = Pattern.compile('.*?monitoring taken at (.*?) on.*')
    protected static final DateTimeFormatter SERVER_TIME_FORMATTER = DateTimeFormatter.ofPattern('M/d/uu h:mm a')
    protected static Long serverTimeShiftMinutes = null

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
            final XnatInterface newInterface = XnatInterface.authenticate(xnatConfig.xnatUrl, user,
                    new XnatConnectionConfig(versionClass: xnatConfig.xnatVersion, logOnValidationFailure: Settings.LOGGING_ALLOWED))
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

    Response getJson(RequestSender request, String url) {
        // request should be a RequestSpecification if it's just credentials or a ResponseSpecification if it's credentials appended with expected response behavior
        final Response response = request.get(url)
        fixContentType(response, ContentType.JSON)
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

    List<File> downloadAllDicomFromSession(User authUser, Project project, Subject subject, ImagingSession session) {
        final XnatInterface authInterface = interfaceFor(authUser)
        final List<File> dicomFiles = []
        final List<Scan> scans = authInterface.readScans(project, subject, session)
        log.info("Downloading DICOM files for list of scans: ${scans}...")
        scans.each { scan ->
            scan.scanResources.each { resource ->
                if (resource.format == 'DICOM') { // scan may have > 1 DICOM resource, "DICOM" and "secondary"
                    log.info("Found DICOM-format resource for scan ${scan.id} with label ${resource.folder}...")
                    resource.resourceFiles.each { dicomFile ->
                        log.info("Downloading local copy of DICOM resource file ${dicomFile.name}...")
                        dicomFiles << saveBinaryResponseToFile(authInterface.queryBase().get(mainInterface().resourceFileUrl(resource, dicomFile)))
                    }
                }
            }
        }
        assertTrue(dicomFiles.size() > 0)
        dicomFiles
    }

    RequestSpecification invalidCredentials() {
        Credentials.build(RandomHelper.randomLetters(12), RandomHelper.randomLetters(12)) // randomly generating this is fine. Probability of collision is astronomically small with 12 letters
    }

    AnonScript getDefaultXnatAnonScript() {
        XnatObjectUtils.anonScriptFromURL(DicomEditVersion.UNSPECIFIED, mainInterface().formatXapiUrl('anonymize/default'), Settings.DEFAULT_XNAT_CONFIG.adminUser)
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
        clearPrearchiveSessionsMatchingFilter(authUser, mainInterface().formatRestUrl("/prearchive/projects/${project.id}"), 'ResultSet.Result.collect { it.url }')
    }

    void clearUnassignedPrearchiveSessions(User authUser, List<String> studyInstanceUIDs) {
        clearPrearchiveSessionsMatchingFilter(authUser, mainInterface().formatRestUrl('/prearchive'), "ResultSet.Result.findAll { it.tag in ${studyInstanceUIDs.collect { "'${it}'" }} && it.project == 'Unassigned' }.url")
    }

    void waitForPrearchiveEmpty(User authUser, Project project, int maximumWait) {
        final StopWatch stopWatch = TimeUtils.launchStopWatch()
        while (true) {
            TimeUtils.checkStopWatch(stopWatch, maximumWait, "Prearchive did not empty for project ${project}")
            if (mainInterface().getPrearchiveEntryCountForProject(project) == 0) {
                return
            } else {
                TimeUtils.sleep(1000)
            }
        }
    }

    void waitForDirectArchiveEmpty(User authUser, Project project, int maximumWait) {
        final StopWatch stopWatch = TimeUtils.launchStopWatch()
        while (true) {
            TimeUtils.checkStopWatch(stopWatch, maximumWait, "Direct archive did not empty for project ${project}")
            SessionData[] results = mainInterface().getDirectArchiveEntriesForProject(project)
            if (results.length == 0) {
                return
            } else {
                TimeUtils.sleep(1000)
            }
        }
    }

    /**
     * Best-effort drain of a project's asynchronous ingest pipeline, for use in test teardown before
     * deleting project data. Clears the prearchive first (so the waits below can't hang on a session that
     * would never auto-archive), then waits for the prearchive and direct-archive queues to empty so no
     * server-side build is still holding files open when the caller deletes them. On NFS-backed storage
     * that race otherwise leaves ".nfsXXXX" placeholders ("Device or resource busy") that break the
     * recursive delete. Never throws: a failure or timeout here must not fail teardown.
     */
    void drainIngestPipeline(User authUser, Project project, int maximumWait = 120) {
        try {
            clearPrearchiveSessions(authUser, project)
            waitForPrearchiveEmpty(authUser, project, maximumWait)
            waitForDirectArchiveEmpty(authUser, project, maximumWait)
        } catch (Throwable t) {
            log.warn("drainIngestPipeline did not fully drain project ${project?.id} before teardown; proceeding anyway", t)
        }
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

    @Deprecated
    String getUserSessionsRestUrl(User user) {
        mainInterface().userSessionsRestUrl(user)
    }

    String siteAnonScriptUrl() {
        mainInterface().legacySiteAnonScriptUrl()
    }

    void validateUpload(User authUser, String fileUrl, File localFile) {
        TestNgUtils.assertBinaryFilesEqual(
                localFile,
                saveBinaryResponseToFile(Credentials.build(authUser).get(fileUrl)))
    }

    void validateResource(User authUser, Resource resource) {
        resource.resourceFiles.each { resourceFile ->
            validateUpload(authUser, mainInterface().resourceFileUrl(resource, resourceFile), resourceFile.extension.javaFile)
        }
    }

    void assertProjectAccessibility(User authUser, Project project, Accessibility accessibility) {
        interfaceFor(authUser).xmlQuery().get(mainInterface().accessibilityRestUrl(project)).then().assertThat().statusCode(200).and().body(equalTo(accessibility.toString()))
    }

    void createProject(User authUser, Project project, File projectXmlFile) {
        new ProjectXMLPutExtension(project, projectXmlFile).create(interfaceFor(authUser))
    }

    Subject createSubject(User authUser, Project project, File subjectXML) {
        final SubjectExtension extension = new SubjectXMLPutExtension(subjectXML)
        extension.create(interfaceFor(authUser), project)
        extension.parentObject
    }

    void deleteProjectSilently(User authUser, Project project) {
        try {
            interfaceFor(authUser).deleteProject(project)
        } catch (Exception | Error ignored) {}
    }

    void initializeXnat() {
        final Response initResponse = adminCredentials.get(interfaceFor(adminUser).formatXapiUrl('/siteConfig/initialized'))
        if (initResponse.statusCode() == 200 && initResponse.as(Boolean)) {
            log.info('XNAT already initialized')
        } else {
            interfaceFor(adminUser).initializeXnat()
        }
    }

    void setupTestUsers() {
        final XnatInterface adminInterface = interfaceFor(adminUser)
        final List<String> allUsers = adminCredentials.get(adminInterface.formatXapiUrl('/users')).path('')

        if (mainUser.username in allUsers) {
            adminInterface.verifyUser(mainUser)
            adminInterface.enableUser(mainUser)
        } else {
            adminInterface.createUser(xnatConfig.mainUser.email(Settings.EMAIL))
        }

        if (mainAdminUser.username in allUsers) {
            adminInterface.verifyUser(mainAdminUser)
            adminInterface.enableUser(mainAdminUser)
            adminInterface.makeUserAdmin(mainAdminUser)
        } else {
            adminInterface.createUser(mainAdminUser.email(Settings.EMAIL))
            adminInterface.makeUserAdmin(mainAdminUser)
        }
    }

    LocalDateTime translateTimeToServerTimezone(LocalDateTime timeToConvert) {
        if (serverTimeShiftMinutes == null) {
            final XnatInterface adminInterface = interfaceFor(mainAdminUser)
            final Matcher serverTimeMatcher = SERVER_TIME_REGEX.matcher(
                    adminInterface
                            .queryBase()
                            .get(adminInterface.formatXnatUrl('/monitoring'))
                            .then()
                            .assertThat()
                            .statusCode(200)
                            .and()
                            .extract()
                            .asString()
            )
            final LocalDateTime now = LocalDateTime.now()
            if (!serverTimeMatcher.find()) {
                fail('Could not parse monitoring servlet.')
            }
            final LocalDateTime extractedTime = LocalDateTime.parse(serverTimeMatcher.group(1), SERVER_TIME_FORMATTER)
            serverTimeShiftMinutes = 15 * (Math.round(ChronoUnit.MINUTES.between(extractedTime, now) / 15)) // the smallest precision of a timezone difference is 15 minutes, we can round to that precision
        }
        timeToConvert.minusMinutes(serverTimeShiftMinutes)
    }

    private void clearPrearchiveSessionsMatchingFilter(User authUser, String prearchiveQueryUrl, String jsonPathFilter) {
        interfaceFor(authUser).jsonQuery().get(prearchiveQueryUrl).then().assertThat().statusCode(200).and().extract().jsonPath().getList(jsonPathFilter).each { deleteUrl ->
            queryBaseFor(authUser).delete(mainInterface().formatRestUrl(deleteUrl as String)).then().assertThat().statusCode(200)
        }
    }

}
