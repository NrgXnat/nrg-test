package org.nrg.testing.listeners.adapters.git

import io.restassured.RestAssured
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import org.apache.log4j.Logger
import org.nrg.testing.CommonStringUtils
import org.nrg.testing.TestNgUtils
import org.nrg.testing.listeners.adapters.BaseTestListener
import org.testng.ITestResult

class GitLogListener extends BaseTestListener {

    private static String USERNAME, PASSWORD, XNAT_URL
    private static final Logger LOGGER = Logger.getLogger(GitLogListener)
    private static boolean initialized = false

    static void init(String username, String password, String xnatUrl) {
        USERNAME = username
        PASSWORD = password
        XNAT_URL = xnatUrl
        final Response response = credentials().post(CommonStringUtils.formatUrl(XNAT_URL, 'xapi/testlog/init'))
        if (response.getStatusCode() == 200) {
            initialized = true
        } else {
            LOGGER.warn("Could not initialize git repos from XNAT logs. Status code: ${response.statusCode}")
        }
    }

    @Override
    void onTestComplete(ITestResult testResult) {
        if (initialized) {
            final Response response = credentials().post(CommonStringUtils.formatUrl(XNAT_URL, 'xapi/testlog/commit', TestNgUtils.getTestName(testResult)))
            if (response.getStatusCode() != 200) {
                LOGGER.warn("Could not commit changes to XNAT log git repos. Status code: ${response.statusCode}")
            }
        }
    }

    @Override
    void onStart(ITestResult testResult) {}

    @Override
    void onFailure(ITestResult testResult) {}

    @Override
    void onSuccess(ITestResult testResult) {}

    @Override
    void onSkipped(ITestResult testResult) {}

    private static RequestSpecification credentials() {
        RestAssured.given().authentication().preemptive().basic(USERNAME, PASSWORD)
    }

}
