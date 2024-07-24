package org.nrg.testing.xnat

import com.fasterxml.jackson.databind.ObjectMapper
import io.restassured.RestAssured
import io.restassured.config.RestAssuredConfig
import io.restassured.path.json.mapper.factory.Jackson2ObjectMapperFactory
import io.restassured.specification.RequestSpecification
import org.apache.commons.io.FileUtils
import org.nrg.testing.enums.TestData
import org.nrg.testing.util.RandomHelper
import org.nrg.testing.xnat.components.ComponentizedTest
import org.nrg.testing.xnat.components.SessionImporterStep
import org.nrg.testing.xnat.components.TestComponent
import org.nrg.testing.xnat.conf.Settings
import org.nrg.testing.xnat.rest.XnatRestDriver
import org.nrg.xnat.pogo.DataType
import org.nrg.xnat.pogo.Project
import org.nrg.xnat.pogo.dicom.DicomScpReceiver
import org.nrg.xnat.pogo.users.User
import org.nrg.xnat.rest.Credentials
import org.nrg.xnat.rest.HttpStatusException
import org.testng.annotations.AfterClass
import org.testng.annotations.AfterSuite
import org.testng.annotations.BeforeSuite

import java.lang.reflect.Type
import java.nio.file.Paths
import java.util.concurrent.Callable

import static io.restassured.config.ObjectMapperConfig.objectMapperConfig

class BaseXnatRestTest extends BaseXnatTest {

    protected static final List<Project> suiteTestProjects = []
    protected final List<Project> testProjects = []
    protected final List<DicomScpReceiver> createdReceivers = new ArrayList<>()
    protected final TestComponent UPLOAD_SAMPLE1_SI = new SessionImporterStep(TestData.SAMPLE_1)

    @BeforeSuite(alwaysRun = true)
    protected void addXnatSerializers() {
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(objectMapperConfig().jackson2ObjectMapperFactory(
                new Jackson2ObjectMapperFactory() {
                    @Override
                    ObjectMapper create(Type type, String s) {
                        XnatRestDriver.XNAT_REST_MAPPER
                    }
                }
        ))
    }

    @AfterSuite(alwaysRun = true)
    protected void removeLongTermProjects() {
        suiteTestProjects.each { project ->
            restDriver.deleteProjectSilently(mainAdminUser, project)
        }
    }

    @AfterSuite(alwaysRun = true)
    protected void deleteDownloadedTempData() {
        FileUtils.deleteDirectory(Paths.get(Settings.TEMP_SUBDIR).toFile())
    }

    @AfterClass(alwaysRun = true)
    protected void removeTempObjects() {
        testProjects.each { project ->
            restDriver.deleteProjectSilently(mainAdminUser, project)
        }
        createdReceivers.each { possiblyCreatedReceiver ->
            try {
                mainAdminInterface().deleteDicomScpReceiver(possiblyCreatedReceiver)
            } catch (Throwable ignored) {
                // we may have allocated the object for it but not created it
            }
        }
    }

    @Override
    protected void setupXnat() {
        restDriver.initializeXnat()
        restDriver.setupTestUsers()
        if (Settings.SETUP_MR_SCAN) {
            restDriver.interfaceFor(restDriver.adminUser).setupDataType(DataType.MR_SCAN)
        }
    }


    protected void createRequiredUsers() {
    }

    @Override
    protected List<User> createGenericUsers(int numUsers) {
        (0 ..< numUsers).collect {
            final User user = Users.genericAccount()
            mainAdminInterface().createUser(user)
            user
        }
    }

    @Deprecated
    protected RequestSpecification mainCredentials() {
        Credentials.build(mainUser)
    }

    @Deprecated
    protected RequestSpecification mainAdminCredentials() {
        Credentials.build(mainAdminUser)
    }

    protected DicomScpReceiver newDefaultReceiver() {
        final DicomScpReceiver receiver = new DicomScpReceiver()
                .host(Settings.DICOM_HOST)
                .aeTitle(RandomHelper.randomID(10))
                .port(Settings.DICOM_PORT)
        createdReceivers << receiver
        receiver
    }

    Project registerTempProject() {
        final Project project = new Project()
        testProjects << project
        project
    }

    static Project registerSuiteTempProject() {
        final Project project = new Project()
        suiteTestProjects << project
        project
    }

    protected RequestSpecification mainQueryBase() {
        restDriver.mainQueryBase()
    }

    protected RequestSpecification mainAdminQueryBase() {
        mainAdminInterface().queryBase()
    }

    protected String formatRestUrl(String... objects) {
        mainInterface().formatRestUrl(objects)
    }

    protected String formatXnatUrl(String... objects) {
        mainInterface().formatXnatUrl(objects)
    }

    protected String formatXapiUrl(String... objects) {
        mainInterface().formatXapiUrl(objects)
    }

    protected void run(ComponentizedTest test) {
        test.run(this)
    }

    protected static TestComponent expectStatusCode(TestComponent baseAction, int... expectedStatusCodes) {
        new TestComponent() {
            @Override
            void perform(BaseXnatRestTest xnatRestTest, Project project) {
                try {
                    baseAction.perform(xnatRestTest, project)
                    throw new RuntimeException("${baseAction.class.simpleName ?: '[anonymous class]'} action attempt should have failed!")
                } catch (HttpStatusException encountered) {
                    if (!(encountered.statusCode in expectedStatusCodes)) {
                        throw encountered
                    }
                }
            }
        }
    }

    protected static TestComponent expect403(TestComponent baseAction) {
        expectStatusCode(baseAction, 403)
    }

    protected static TestComponent expect404(TestComponent baseAction) {
        expectStatusCode(baseAction, 404)
    }

    protected static TestComponent expectPermissionsIssue(TestComponent baseAction) {
        expectStatusCode(baseAction, 401, 403)
    }

    protected void expectStatusCode(Runnable action, int... expectedStatusCode) {
        expectStatusCode(
                new TestComponent() {
                    @Override
                    void perform(BaseXnatRestTest xnatRestTest, Project project) {
                        action.run()
                    }
                },
                expectedStatusCode
        ).perform(this, null)
    }

    protected void expectStatusCode(Callable<Void> action, int... expectedStatusCode) {
        expectStatusCode(
                new TestComponent() {
                    @Override
                    void perform(BaseXnatRestTest xnatRestTest, Project project) {
                        action.call()
                    }
                },
                expectedStatusCode
        ).perform(this, null)
    }

    protected void expect403(Runnable action) {
        expectStatusCode(action, 403)
    }

    protected void expect403(Callable<Void> action) {
        expectStatusCode(action, 403)
    }

    protected void expect404(Runnable action) {
        expectStatusCode(action, 404)
    }

    protected void expect404(Callable<Void> action) {
        expectStatusCode(action, 404)
    }

    protected void expectPermissionsIssue(Runnable action) {
        expectStatusCode(action, 401, 403)
    }

    protected void expectPermissionsIssue(Callable<Void> action) {
        expectStatusCode(action, 401, 403)
    }

}
