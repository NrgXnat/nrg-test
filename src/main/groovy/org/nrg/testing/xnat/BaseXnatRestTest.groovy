package org.nrg.testing.xnat

import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.restassured.RestAssured
import com.jayway.restassured.config.RestAssuredConfig
import com.jayway.restassured.config.SSLConfig
import com.jayway.restassured.mapper.factory.Jackson2ObjectMapperFactory
import com.jayway.restassured.specification.RequestSpecification
import org.nrg.testing.xnat.conf.Settings
import org.nrg.testing.xnat.rest.XnatRestDriver
import org.nrg.xnat.pogo.DataType
import org.nrg.xnat.pogo.users.User
import org.nrg.xnat.rest.Credentials
import org.testng.annotations.BeforeSuite

import static com.jayway.restassured.config.ObjectMapperConfig.objectMapperConfig

class BaseXnatRestTest extends BaseXnatTest {



    @BeforeSuite
    protected void addXnatSerializers() {
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(objectMapperConfig().jackson2ObjectMapperFactory(
                new Jackson2ObjectMapperFactory() {
                    @Override
                    ObjectMapper create(Class aClass, String s) {
                        XnatRestDriver.XNAT_REST_MAPPER
                    }
                }
        )).sslConfig(SSLConfig.sslConfig().relaxedHTTPSValidation('SSL'))
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
            restDriver.createUser(user)
            user
        }
    }

    protected RequestSpecification mainCredentials() {
        Credentials.build(mainUser)
    }

    protected RequestSpecification mainAdminCredentials() {
        Credentials.build(mainAdminUser)
    }

    protected RequestSpecification mainQueryBase() {
        restDriver.mainQueryBase()
    }

    protected String formatRestUrl(String... objects) {
        restDriver.formatRestUrl(objects)
    }

}
