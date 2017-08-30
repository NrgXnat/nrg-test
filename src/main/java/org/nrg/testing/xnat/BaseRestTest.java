package org.nrg.testing.xnat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.RestAssuredConfig;
import com.jayway.restassured.mapper.factory.Jackson2ObjectMapperFactory;
import com.jayway.restassured.specification.RequestSpecification;
import org.nrg.testing.xnat.rest.XnatRestDriver;
import org.nrg.xnat.pogo.users.User;
import org.nrg.xnat.rest.Credentials;
import org.testng.annotations.BeforeSuite;

import java.util.ArrayList;
import java.util.List;

import static com.jayway.restassured.config.ObjectMapperConfig.objectMapperConfig;

public class BaseRestTest extends BaseXnatTest {

    @BeforeSuite
    protected void addXnatSerializers() {
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(objectMapperConfig().jackson2ObjectMapperFactory(
                new Jackson2ObjectMapperFactory() {
                    @Override
                    public ObjectMapper create(Class aClass, String s) {
                        return XnatRestDriver.XNAT_REST_MAPPER;
                    }
                }
        ));
    }

    @Override
    protected void setupXnat() {
        restDriver.initializeXnat();
        restDriver.setupTestUsers();
    }

    @Override
    protected List<User> createGenericUsers(int numUsers) {
        final List<User> users = new ArrayList<>();

        for (int i = 0; i < numUsers; i++) {
            final User user = Users.genericAccount(restDriver);
            restDriver.createUser(user);
            users.add(user);
        }
        return users;
    }

    protected RequestSpecification mainCredentials() {
        return Credentials.build(mainUser);
    }

    protected RequestSpecification mainAdminCredentials() {
        return Credentials.build(mainAdminUser);
    }

    protected String formatRestUrl(String... objects) {
        return restDriver.formatRestUrl(objects);
    }

}
