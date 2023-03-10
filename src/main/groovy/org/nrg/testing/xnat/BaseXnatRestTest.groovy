package org.nrg.testing.xnat

import com.fasterxml.jackson.databind.ObjectMapper
import io.restassured.RestAssured
import io.restassured.config.RestAssuredConfig
import io.restassured.path.json.mapper.factory.Jackson2ObjectMapperFactory
import io.restassured.specification.RequestSpecification
import org.nrg.testing.enums.TestData
import org.nrg.testing.xnat.components.ComponentizedTest
import org.nrg.testing.xnat.components.SessionImporterStep
import org.nrg.testing.xnat.components.TestComponent
import org.nrg.testing.xnat.conf.Settings
import org.nrg.testing.xnat.rest.XnatRestDriver
import org.nrg.xnat.pogo.DataType
import org.nrg.xnat.pogo.Project
import org.nrg.xnat.pogo.users.User
import org.nrg.xnat.rest.Credentials
import org.nrg.xnat.rest.ForbiddenException
import org.nrg.xnat.rest.NotFoundException
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeSuite

import java.lang.reflect.Type
import java.util.concurrent.Callable

import static io.restassured.config.ObjectMapperConfig.objectMapperConfig
import static org.hamcrest.MatcherAssert.assertThat
import static org.testng.AssertJUnit.fail

class BaseXnatRestTest extends BaseXnatTest {

    protected final List<Project> testProjects = []
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

    @AfterClass(alwaysRun = true)
    protected void removeTempProjects() {
        testProjects.each { project ->
            restDriver.deleteProjectSilently(mainAdminUser, project)
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

    Project registerTempProject() {
        final Project project = new Project()
        testProjects << project
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

    protected static TestComponent expect403(TestComponent baseAction) {
        new TestComponent() {
            @Override
            void perform(BaseXnatRestTest xnatRestTest, Project project) {
                try {
                    baseAction.perform(xnatRestTest, project)
                    throw new RuntimeException("${baseAction.class.simpleName ?: '[anonymous class]'} action attempt should have failed!")
                } catch (ForbiddenException ignored) {
                    // expected
                }
            }
        }
    }

    protected static TestComponent expect404(TestComponent baseAction) {
        new TestComponent() {
            @Override
            void perform(BaseXnatRestTest xnatRestTest, Project project) {
                try {
                    baseAction.perform(xnatRestTest, project)
                    throw new RuntimeException("${baseAction.class.simpleName ?: '[anonymous class]'} action attempt should have failed!")
                } catch (NotFoundException ignored) {
                    // expected
                }
            }
        }
    }

    protected void expect403(Runnable action) {
        expect403(
                new TestComponent() {
                    @Override
                    void perform(BaseXnatRestTest xnatRestTest, Project project) {
                        action.run()
                    }
                }
        ).perform(this, null)
    }

    protected void expect403(Callable<Void> action) {
        expect403(
                new TestComponent() {
                    @Override
                    void perform(BaseXnatRestTest xnatRestTest, Project project) {
                        action.call()
                    }
                }
        ).perform(this, null)
    }

    protected void expect404(Runnable action) {
        expect404(
                new TestComponent() {
                    @Override
                    void perform(BaseXnatRestTest xnatRestTest, Project project) {
                        action.run()
                    }
                }
        ).perform(this, null)
    }

    protected void expect404(Callable<Void> action) {
        expect404(
                new TestComponent() {
                    @Override
                    void perform(BaseXnatRestTest xnatRestTest, Project project) {
                        action.call()
                    }
                }
        ).perform(this, null)
    }

}
