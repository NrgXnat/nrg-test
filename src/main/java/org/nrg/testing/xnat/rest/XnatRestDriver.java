package org.nrg.testing.xnat.rest;

import com.jayway.restassured.internal.RestAssuredResponseImpl;
import com.jayway.restassured.path.json.exception.JsonPathException;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSender;
import com.jayway.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.http.entity.ContentType;
import org.apache.log4j.Logger;
import org.nrg.jira.testing_components.TestStatus;
import org.nrg.testing.CommonUtils;
import org.nrg.testing.TestController;
import org.nrg.testing.auth.Credentials;
import org.nrg.testing.enums.TestData;
import org.nrg.testing.util.RandomHelper;
import org.nrg.testing.xnat.XnatAliasToken;
import org.nrg.testing.xnat.conf.Settings;
import org.nrg.testing.xnat.conf.XnatConfig;
import org.nrg.testing.xnat.versions.XnatVersion;
import org.nrg.testing.xnat.versions.XnatVersionList;
import org.nrg.xnat.pojo.Project;
import org.nrg.xnat.pojo.Subject;
import org.nrg.xnat.pojo.User;
import org.nrg.xnat.pojo.experiments.ImagingSession;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.AssertJUnit.fail;

public abstract class XnatRestDriver {

    protected XnatConfig xnatConfig;
    protected TestController testController;
    protected static final Logger LOGGER = Logger.getLogger(XnatRestDriver.class);

    public abstract List<Class<? extends XnatVersion>> getHandledVersions();

    public static XnatRestDriver getInstance(XnatConfig xnatConfig) {
        XnatVersionList.readXnatVersions();

        final XnatConfig config = (xnatConfig != null) ? xnatConfig : Settings.DEFAULT_XNAT_CONFIG;
        if (XnatVersionList.KNOWN_VERSION_KEYS.contains(config.getXnatVersion())) {
            try {
                final XnatRestDriver restDriver = XnatVersionList.KNOWN_KEY_REST_DRIVER_MAP.get(config.getXnatVersion()).newInstance();
                restDriver.setXnatConfig(config);
                return restDriver;
            } catch (Exception e) {
                throw new RuntimeException("Could not construct XnatRestDriver class due to:", e);
            }
        } else {
            throw new RuntimeException(String.format("Could not find requested version of XNAT (%s) in list of available versions.", config.getXnatVersion()));
        }
    }

    public static XnatRestDriver getInstance() {
        return getInstance(Settings.DEFAULT_XNAT_CONFIG);
    }

    public void setTestController(TestController testController) {
        this.testController = testController;
    }

    public void setXnatConfig(XnatConfig config) {
        xnatConfig = config;
    }

    public XnatConfig getXnatConfig() {
        return xnatConfig;
    }

    public void captureStep(TestStatus status, String comment) {
        if (testController.isTestRunning()) {
            testController.getCurrentTest().updateStepResult(testController.getStepCounter().getValue(), status, comment);
            testController.getStepCounter().increment();
        }
    }

    public void passStep(String comment) {
        captureStep(TestStatus.PASS, comment);
    }

    public void passStep() {
        passStep(null);
    }

    public String formatXnatUrl(String... components) {
        return CommonUtils.formatUrl(xnatConfig.getXnatUrl(), CommonUtils.formatUrl((Object[]) components));
    }

    public String formatRestUrl(String... components) {
        return formatXnatUrl("data", CommonUtils.formatUrl((Object[]) components));
    }

    public String formatXapiUrl(String... components) {
        return formatXnatUrl("xapi", CommonUtils.formatUrl((Object[]) components));
    }

    public Response getJson(RequestSender request, String url) {
        // request should be a RequestSpecification if it's just credentials or a ResponseSpecification if it's credentials appended with expected response behavior
        final Response response = request.get(url);
        fixContentType(response, ContentType.APPLICATION_JSON);
        return response;
    }

    public static void fixContentType(Response restResponse, ContentType type) {
        ((RestAssuredResponseImpl) restResponse).setContentType(type.getMimeType()); // XNAT is returning the wrong content type in some cases
    }

    public RequestSpecification mainCredentials() {
        return xnatConfig.getMainCredentials();
    }

    public RequestSpecification mainAdminCredentials() {
        return xnatConfig.getMainAdminCredentials();
    }

    public RequestSpecification adminCredentials() {
        return xnatConfig.getAdminCredentials();
    }


    public String permuteSeleniumEmail() {
        List<String> emailList = adminCredentials().when().get(formatRestUrl("users")).then().extract().path("ResultSet.Result.email");
        String permutedEmail = generateEmailPermutation();
        while (emailList.contains(permutedEmail)) {
            // regenerate as necessary until unique
            permutedEmail = generateEmailPermutation();
        }
        return permutedEmail;
    }

    private String generateEmailPermutation() {
        String emailID = Settings.EMAIL.substring(0, Settings.EMAIL.indexOf("@"));
        String emailProvider = Settings.EMAIL.substring(Settings.EMAIL.indexOf("@"));
        String permutedEmail = "";
        for (int i = 0; i < emailID.length() - 1; i++) {
            // trailing periods are not allowed
            permutedEmail += Settings.EMAIL.charAt(i) + RandomHelper.randomPeriods();
        }
        return permutedEmail + emailID.substring(emailID.length() - 1) + emailProvider; // Don't forget the last character of the email!
    }

    public String aliasTokenUrl() {
        return formatRestUrl("services", "tokens", "issue");
    }

    public XnatAliasToken generateAliasToken(RequestSpecification authRequest) {
        Response response = getJson(authRequest.expect().statusCode(200), aliasTokenUrl());
        return new XnatAliasToken(response.jsonPath().getString("alias"), response.jsonPath().getString("secret"));
    }
    
    public String projectExperimentsUrl(Project project) {
        return formatRestUrl("projects", project.getId(), "experiments");
    }

    public String getAccessionNumber(ImagingSession session, RequestSpecification credentials) {
        if (session.getPrimaryProject() == null) throw new IllegalArgumentException("Session object must have project specified.");

        return credentials.get(projectExperimentsUrl(session.getPrimaryProject())).then().extract().jsonPath().param("session", session.getLabel()).getString("ResultSet.Result.find {it.label == session }.ID");
    }

    public String getAccessionNumber(ImagingSession session) {
        return getAccessionNumber(session, mainCredentials());
    }

    public void waitForAutoRun(int maximumTime, ImagingSession session, RequestSpecification credentials) {
        final String accessionNumber = getAccessionNumber(session, credentials);

        final StopWatch stopWatch = CommonUtils.launchStopWatch();
        while (true) {
            CommonUtils.checkStopWatch(stopWatch, maximumTime, "AutoRun did not complete in allotted number of seconds: " + maximumTime);

            final String status = credentials.given().queryParam("experiment", accessionNumber).queryParam("format", "json").
                    get(formatRestUrl("services/workflows/AutoRun")).then().extract().jsonPath().getString("items.get(0).data_fields.status");

            if (status.equals("Complete")) {
                return;
            } else if (status.equals("Failed")) {
                fail("AutoRun failed.");
            }
            CommonUtils.sleep(1000);
        }
    }

    public void waitForAutoRun(ImagingSession session) {
        waitForAutoRun(60, session, mainCredentials());
    }

    public String getBuildInfo() {
        try {
            Response buildResponse = getJson(mainCredentials(), formatXapiUrl("siteConfig", "buildInfo"));
            if (buildResponse.statusCode() != 200) {
                LOGGER.info("Couldn't get build information. Status code: " + buildResponse.statusCode());
                return null;
            }
            return String.format("Version %s (commit %s)", buildResponse.then().extract().path("version"), buildResponse.then().extract().path("commit"));
        } catch (JsonPathException jpe) {
            LOGGER.info("Couldn't get build information because of JsonPathException (likely means site has not been initialized).");
        } catch (Exception e) {
            LOGGER.warn("Couldn't get build information because of: ", e);
        }
        return null;
    }


    public void uploadToSessionZipImporter(User authUser, File sessionZip, Project project, Subject subject, ImagingSession session) {
        if (project == null) {
            throw new RuntimeException("Project cannot be null when uploading to zip importer.");
        }

        final Map<String, String> queryPararms = new HashMap<>();
        queryPararms.put("dest", "/archive");
        queryPararms.put("PROJECT_ID", project.getId());

        if (subject != null) queryPararms.put("SUBJECT_ID", subject.getLabel());
        if (session != null) queryPararms.put("EXPT_LABEL", session.getLabel());

        final RequestSpecification effectiveCredentials = (authUser == null) ? Credentials.build(xnatConfig.getMainUser()) : Credentials.build(authUser);

        effectiveCredentials.given().multiPart(sessionZip).queryParameters(queryPararms).when().post(formatRestUrl("/services/import")).then().statusCode(200);
    }

    public void uploadToSessionZipImporter(File sessionZip, Project project) {
        uploadToSessionZipImporter(null, sessionZip, project, null, null);
    }

    public void uploadToSessionZipImporter(TestData testData, Project project) {
        uploadToSessionZipImporter(testData.toFile(), project);
    }

    public void uploadToSessionZipImporter(File sessionZip, ImagingSession session) {
        if (session.getPrimaryProject() == null) throw new IllegalArgumentException("Session must have project object specified to use this shortcut method");
        uploadToSessionZipImporter(null, sessionZip, session.getPrimaryProject(), session.getSubject(), session);
    }

    public String getUserSessionsRestUrl(User user) {
        return formatRestUrl("user", user.getUsername(), "sessions");
    }

    public void expireAllActiveSessions(User authUser, User targetUser) {
        Credentials.build(authUser).expect().statusCode(200).when().delete(getUserSessionsRestUrl(targetUser));
    }

    public int getNumberActiveSessions(User user) {
        return (int) Credentials.build(user).expect().statusCode(200).when().get(getUserSessionsRestUrl(user)).then().extract().path(user.getUsername());
    }

}
