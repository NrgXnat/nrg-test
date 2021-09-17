package org.nrg.testing.xnat

import com.fasterxml.jackson.databind.ObjectMapper
import io.restassured.RestAssured
import io.restassured.config.RestAssuredConfig
import io.restassured.path.json.mapper.factory.Jackson2ObjectMapperFactory
import io.restassured.specification.RequestSpecification
import org.nrg.testing.xnat.conf.Settings
import org.nrg.testing.xnat.rest.XnatRestDriver
import org.nrg.xnat.pogo.DataType
import org.nrg.xnat.pogo.users.User
import org.nrg.xnat.rest.Credentials
import org.testng.annotations.BeforeSuite

import java.lang.reflect.Type

import static io.restassured.config.ObjectMapperConfig.objectMapperConfig

class BaseXnatRestTest extends BaseXnatTest {

    @BeforeSuite
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

}
