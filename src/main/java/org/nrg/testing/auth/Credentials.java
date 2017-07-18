package org.nrg.testing.auth;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.specification.AuthenticationSpecification;
import com.jayway.restassured.specification.RequestSpecification;
import org.nrg.testing.xnat.XnatAliasToken;
import org.nrg.xnat.pojo.users.User;

public class Credentials {

    public static RequestSpecification build(String username, String password, boolean preemptive) {
        if (username == null || password == null) return null;

        final AuthenticationSpecification credentials = RestAssured.given().authentication();
        return (preemptive) ? credentials.preemptive().basic(username, password) : credentials.basic(username, password);
    }

    public static RequestSpecification build(String username, String password) {
        return build(username, password, true);
    }

    public static RequestSpecification build(XnatAliasToken aliasToken) {
        return (aliasToken == null) ? null : build(aliasToken.getAlias(), aliasToken.getSecret());
    }

    public static RequestSpecification build(User xnatUser) {
        return build(xnatUser.getUsername(), xnatUser.getPassword());
    }

}
