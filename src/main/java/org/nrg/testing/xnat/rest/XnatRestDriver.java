package org.nrg.testing.xnat.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.RestAssuredConfig;
import com.jayway.restassured.internal.RestAssuredResponseImpl;
import com.jayway.restassured.mapper.factory.Jackson2ObjectMapperFactory;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.path.json.config.JsonPathConfig;
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
import org.nrg.testing.xnat.extensions.ResourceFileExtension;
import org.nrg.testing.xnat.versions.XnatVersion;
import org.nrg.testing.xnat.versions.XnatVersionList;
import org.nrg.xnat.jackson.mappers.XnatRestReadObjectMapper;
import org.nrg.xnat.jackson.mappers.XnatRestReadWriteObjectMapper;
import org.nrg.xnat.pojo.Investigator;
import org.nrg.xnat.pojo.Project;
import org.nrg.xnat.pojo.Subject;
import org.nrg.xnat.pojo.experiments.ImagingSession;
import org.nrg.xnat.pojo.experiments.NonimagingAssessor;
import org.nrg.xnat.pojo.experiments.SubjectAssessor;
import org.nrg.xnat.pojo.resources.Resource;
import org.nrg.xnat.pojo.resources.ResourceFile;
import org.nrg.xnat.pojo.users.User;
import org.nrg.xnat.pojo.users.UserGroup;

import java.io.File;
import java.util.*;

import static com.jayway.restassured.config.ObjectMapperConfig.objectMapperConfig;
import static com.jayway.restassured.http.ContentType.*;
import static org.testng.AssertJUnit.fail;

public abstract class XnatRestDriver {

    protected XnatConfig xnatConfig;
    protected TestController testController;
    protected static final Logger LOGGER = Logger.getLogger(XnatRestDriver.class);
    public static final ObjectMapper XNAT_REST_MAPPER = new XnatRestReadWriteObjectMapper();

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

    public RequestSpecification invalidCredentials() {
        return Credentials.build(RandomHelper.randomLetters(12), RandomHelper.randomLetters(12)); // randomly generating this is fine. Probability of collision is astronomically small with 12 letters
    }

    public User mainUser() {
        return xnatConfig.getMainUser();
    }

    public User mainAdminUser() {
        return xnatConfig.getMainAdminUser();
    }

    public User adminUser() {
        return xnatConfig.getAdminUser();
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

    public XnatAliasToken generateAliasToken(User user) {
        Response response = getJson(Credentials.build(user).expect().statusCode(200), aliasTokenUrl());
        return new XnatAliasToken(response.jsonPath().getString("alias"), response.jsonPath().getString("secret"));
    }
    
    public String projectExperimentsUrl(Project project) {
        return formatRestUrl("projects", project.getId(), "experiments");
    }

    public String getAccessionNumber(User authUser, ImagingSession session) {
        if (session.getPrimaryProject() == null) throw new IllegalArgumentException("Session object must have project specified.");

        return Credentials.build(authUser).get(projectExperimentsUrl(session.getPrimaryProject())).then().extract().jsonPath().param("session", session.getLabel()).getString("ResultSet.Result.find {it.label == session }.ID");
    }

    public String getAccessionNumber(ImagingSession session) {
        return getAccessionNumber(mainUser(), session);
    }

    public void waitForAutoRun(User authUser, int maximumTime, ImagingSession session) {
        final String accessionNumber = getAccessionNumber(authUser, session);

        final StopWatch stopWatch = CommonUtils.launchStopWatch();
        while (true) {
            CommonUtils.checkStopWatch(stopWatch, maximumTime, "AutoRun did not complete in allotted number of seconds: " + maximumTime);

            final String status = Credentials.build(authUser).given().queryParam("experiment", accessionNumber).queryParam("format", "json").
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
        waitForAutoRun(mainUser(), 60, session);
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
        uploadToSessionZipImporter(mainUser(), sessionZip, project, null, null);
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

    public void initializeXnat() {
        final Response initResponse = adminCredentials().get(formatXapiUrl("siteConfig/initialized"));
        if (initResponse.statusCode() == 200 && initResponse.then().extract().as(Boolean.class)) {
            LOGGER.info("XNAT already initialized");
        } else {
            adminCredentials().expect().statusCode(200).given().contentType(JSON).body(Collections.singletonMap("initialized", true)).post(formatXapiUrl("siteConfig"));
        }
    }

    public void setupTestUsers() {
        final List<String> allUsers = adminCredentials().get(formatXapiUrl("users")).path("");

        if (allUsers.contains(mainUser().getUsername())) {
            verifyUser(adminUser(), mainUser());
            enableUser(adminUser(), mainUser());
        } else {
            createUser(xnatConfig.getMainUser());
        }

        if (allUsers.contains(mainAdminUser().getUsername())) {
            verifyUser(adminUser(), mainAdminUser());
            enableUser(adminUser(), mainAdminUser());
            makeUserAdmin(adminUser(), mainAdminUser());
        } else {
            createUser(xnatConfig.getMainAdminUser());
        }
    }

    public void createUser(User user) {
        adminCredentials().expect().statusCode(201).given().contentType(JSON).body(user).post(formatXapiUrl("users"));
        if (user.isAdmin()) {
            makeUserAdmin(xnatConfig.getAdminUser(), user);
        }
    }

    public void assignUserToRoles(User authUser, User targetUser, String... roles) {
        Credentials.build(authUser).expect().statusCode(200).given().contentType(JSON).body(roles).put(formatXapiUrl("users", targetUser.getUsername(), "roles"));
    }

    public void addUserToGroups(User authUser, User targetUser, String... groups) {
        Credentials.build(authUser).expect().statusCode(200).given().contentType(JSON).body(groups).put(formatXapiUrl("users", targetUser.getUsername(), "groups"));
    }

    public void verifyUser(User authUser, User targetUser) {
        Credentials.build(authUser).expect().statusCode(200).put(formatXapiUrl("users", targetUser.getUsername(), "verified/true"));
        targetUser.verified(true);
    }

    public void enableUser(User authUser, User targetUser) {
        Credentials.build(authUser).expect().statusCode(200).put(formatXapiUrl("users", targetUser.getUsername(), "enabled/true"));
        targetUser.enabled(true);
    }

    public void makeUserAdmin(User authUser, User targetUser) {
        assignUserToRoles(authUser, targetUser, "Administrator");
        addUserToGroups(authUser, targetUser, "ALL_DATA_ADMIN");
        targetUser.admin(true);
    }

    public void createInvestigators(User authUser, List<Investigator> investigators) {
        final Investigator[] knownInvestigators = Credentials.build(authUser).get(formatXapiUrl("investigators")).as(Investigator[].class);

        for (Investigator investigator : investigators) {
            boolean createRequired = true;

            for (Investigator possibleMatch : knownInvestigators) {
                if (possibleMatch.equals(investigator)) {
                    investigator.id(possibleMatch.getXnatInvestigatordataId());
                    createRequired = false;
                    break;
                }
            }

            if (createRequired) {
                createInvestigator(authUser, investigator);
            }
        }
    }

    public void createInvestigator(User authUser, Investigator investigator) {
        investigator.setXnatInvestigatordataId(
                Credentials.build(authUser).given().contentType(JSON).body(investigator).post(formatXapiUrl("investigators")).jsonPath().getInt("xnatInvestigatordataId")
        );
    }

    public String getProjectCreateUrl(Project project) {
        return formatRestUrl("projects", project.getId());
    }

    public void addUsersToProject(User authUser, Project project) {
        for (Map.Entry<UserGroup, List<User>> userGroupEntry : project.getUsers().entrySet()) {
            final UserGroup group = userGroupEntry.getKey();
            for (User user : userGroupEntry.getValue()) {
                addUserToGroups(authUser, user, String.format("%s_%s", project, group.singularName().toLowerCase())); // TODO: does this work for Custom User groups?
            }
        }
    }

    public void uploadResources(User authUser, List<Resource> resources) {
        for (Resource resource : resources) {
            Credentials.build(authUser).expect().statusCode(200).when().put(formatXnatUrl(resource.resourceUrl(), "resources", resource.getFolder()));
            for (ResourceFile file : resource.getResourceFiles()) {
                if (file.getExtension() == null || !(file.getExtension() instanceof ResourceFileExtension)) {
                    throw new UnsupportedOperationException("ResourceFile must have extension set of type ResourceFileExtension in order to locate file for upload.");
                } else {
                    Credentials.build(authUser).expect().statusCode(200).given().
                            queryParams(SerializationUtils.serializeToMap(file)).multiPart(((ResourceFileExtension) file.getExtension()).getFile()).
                            put(formatXnatUrl(resource.resourceUrl(), "resources", resource.getFolder(), "files"));
                }
            }
        }
    }

    public void createProject(User authUser, Project project) {
        final List<Investigator> investigators = new ArrayList<>();
        investigators.addAll(project.getInvestigators());
        if (project.getPi() != null) investigators.add(project.getPi());
        createInvestigators(authUser, investigators);

        Credentials.build(authUser).expect().statusCode(200).given().queryParameters(SerializationUtils.serializeToMap(project)).put(getProjectCreateUrl(project));
        addUsersToProject(authUser, project);
        for (Resource resource : project.getProjectResources()) {
            resource.setProject(project);
        }
        uploadResources(authUser, project.getProjectResources());

        for (Subject subject : project.getSubjects()) {
            createSubject(authUser, subject.project(project));
        }
    }

    public void createSubject(User authUser, Project project, Subject subject) {
        Credentials.build(authUser).expect().statusCode(201).given().queryParams(SerializationUtils.serializeToMap(subject)).
                put(formatRestUrl("projects", project.getId(), "subjects", subject.getLabel()));
        for (Resource resource : subject.getResources()) {
            resource.project(project).subject(subject);
        }
        uploadResources(authUser, subject.getResources());

        if (!subject.getExperiments().isEmpty()) {
            final Collection<ImagingSession> sessions = CommonUtils.ofType(subject.getExperiments(), ImagingSession.class);
            final Collection<NonimagingAssessor> nonimagingAssessors = CommonUtils.ofType(subject.getExperiments(), NonimagingAssessor.class);

            for (ImagingSession session : sessions) {
                createSession(authUser, project, subject, session);
            }

            for (NonimagingAssessor assessor : nonimagingAssessors) {
                createNonimagingAssessor(authUser, project, subject, assessor);
            }
        }
    }

    public void createSubject(User authUser, Subject subject) {
        if (subject.getProject() == null) {
            throw new UnsupportedOperationException("Subject object must have Project object populated to use this shortcut method");
        }

        createSubject(authUser, subject.getProject(), subject);
    }

    public void createSession(User authUser, Project project, Subject subject, ImagingSession session) {
        if (session.getDataType() == null) {
            throw new UnsupportedOperationException("SubjectAssessor must have xsiType to pass to XNAT for this method");
        }

        session.accessionNumber(
                Credentials.build(authUser).expect().statusCode(201).given().queryParams(SerializationUtils.serializeToMap(session)).
                put(formatRestUrl("projects", project.getId(), "subjects", subject.getLabel(), "experiments", session.getLabel())).asString()
        );
        for (Resource resource : session.getResources()) {
            resource.project(project).subject(subject).subjectAssessor(session);
        }
        uploadResources(authUser, session.getResources());
        // TODO: scans, session assessors ?
    }

    public void createSession(User authUser, ImagingSession session) {
        if (session.getPrimaryProject() == null || session.getSubject() == null) {
            throw new UnsupportedOperationException("SubjectAssessor object must have both Project and Subject to use shortcut method");
        }

        createSession(authUser, session.getPrimaryProject(), session.getSubject(), session);
    }

    public void createNonimagingAssessor(User authUser, Project project, Subject subject, NonimagingAssessor subjectAssessor) {
        // TODO
    }

    public void createNonimagingAssessor(User authUser, NonimagingAssessor subjectAssessor) {
        // TODO
    }

    public String projectDeleteUrl(Project project) {
        return formatRestUrl("projects", project.getId());
    }

    public void deleteProject(User authUser, Project project) {
        Credentials.build(authUser).expect().statusCode(200).given().queryParam("removeFiles", true).delete(projectDeleteUrl(project));
    }

}
