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
import org.nrg.xnat.rest.Credentials
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
            final XnatInterface newInterface = XnatInterface.authenticate(xnatConfig.xnatUrl, user, new XnatConnectionConfig(versionClass: xnatConfig.xnatVersion))
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
            if (interfaceFor(authUser).jsonQuery().get(mainInterface().formatRestUrl("/prearchive/projects/${project.id}")).jsonPath().getInt('ResultSet.Result.size()') == 0) {
                return
            } else {
                TimeUtils.sleep(1000)
            }
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

    private void clearPrearchiveSessionsMatchingFilter(User authUser, String prearchiveQueryUrl, String jsonPathFilter) {
        interfaceFor(authUser).jsonQuery().get(prearchiveQueryUrl).then().assertThat().statusCode(200).and().extract().jsonPath().getList(jsonPathFilter).each { deleteUrl ->
            queryBaseFor(authUser).delete(mainInterface().formatRestUrl(deleteUrl as String)).then().assertThat().statusCode(200)
        }
    }

}
