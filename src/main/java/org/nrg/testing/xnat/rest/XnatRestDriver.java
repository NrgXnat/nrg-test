package org.nrg.testing.xnat.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.internal.RestAssuredResponseImpl;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.path.json.exception.JsonPathException;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSender;
import com.jayway.restassured.specification.RequestSpecification;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.http.entity.ContentType;
import org.apache.log4j.Logger;
import org.nrg.jira.testing_components.TestStatus;
import org.nrg.testing.CommonUtils;
import org.nrg.testing.TestController;
import org.nrg.testing.auth.Credentials;
import org.nrg.testing.enums.TestData;
import org.nrg.testing.file.FileIO;
import org.nrg.testing.util.RandomHelper;
import org.nrg.testing.util.TestNgUtils;
import org.nrg.testing.xnat.XnatAliasToken;
import org.nrg.testing.xnat.conf.Settings;
import org.nrg.testing.xnat.conf.XnatConfig;
import org.nrg.testing.xnat.extensions.*;
import org.nrg.testing.xnat.versions.XnatVersion;
import org.nrg.testing.xnat.versions.XnatVersionList;
import org.nrg.xnat.enums.Accessibility;
import org.nrg.xnat.enums.PrearchiveCode;
import org.nrg.xnat.jackson.mappers.XnatRestReadWriteObjectMapper;
import org.nrg.xnat.pojo.*;
import org.nrg.xnat.pojo.experiments.*;
import org.nrg.xnat.pojo.resources.Resource;
import org.nrg.xnat.pojo.resources.ResourceFile;
import org.nrg.xnat.pojo.users.User;
import org.nrg.xnat.pojo.users.UserGroup;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.*;

import static com.jayway.restassured.http.ContentType.*;
import static org.hamcrest.CoreMatchers.equalTo;
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

    public <T> T parseJsonTable(Response response) {
        return response.jsonPath().get("ResultSet.Result");
    }

    public void saveBinaryResponseToFile(Response response, File file) {
        response.then().assertThat().statusCode(200);
        InputStream inputStream = response.asInputStream();
        try {
            FileUtils.copyInputStreamToFile(inputStream, file);
        } catch (IOException ioe) {
            throw new RuntimeException("Could not download data and copy to file");
        }
    }

    public File saveBinaryResponseToFile(Response response) {
        final File downloadedFile = Paths.get(Settings.TEMP_SUBDIR, RandomHelper.randomID() + ".binarytestfile").toFile();
        saveBinaryResponseToFile(response, downloadedFile);
        return downloadedFile;
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

    public Subject readSubject(User authUser, String accessionNumber) {
        final Response response = Credentials.build(authUser).given().queryParam("format", "json").get(formatRestUrl("subjects", accessionNumber));
        final Subject subject = response.jsonPath().getObject("items.get(0).children.find { it.field == 'demographics' }.items.get(0).data_fields", Subject.class);
        subject.setLabel(response.jsonPath().getString("items.get(0).data_fields.label"));
        return subject;
    }

    public <T extends Experiment> T readExperiment(User authUser, String accessionNumber, Class<T> tClass) {
        final Response response = Credentials.build(authUser).given().queryParam("format", "json").get(formatRestUrl("experiments", accessionNumber));
        final T experiment = response.jsonPath().getObject("items.get(0).data_fields", tClass);
        experiment.setDataType(DataType.lookup(response.jsonPath().getString("items.get(0).meta.'xsi:type'")));
        return experiment;
    }

    public void waitForAutoRun(User authUser, int maximumTime, ImagingSession session) {
        final String accessionNumber = (session.getAccessionNumber() != null) ? session.getAccessionNumber() : getAccessionNumber(authUser, session);

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

    public void clearPrearchiveSessions(User authUser, Project project) {
        final JsonPath jsonPath = Credentials.build(authUser).given().queryParam("format", "json").get(formatRestUrl("prearchive/projects", project.getId())).
                then().assertThat().statusCode(200).and().extract().jsonPath().setRoot("ResultSet.Result");

        final List<String> deleteUrls = jsonPath.getList("collect { it.url }");

        for (String deleteUrl : deleteUrls) {
            Credentials.build(authUser).delete(formatRestUrl(deleteUrl)).then().assertThat().statusCode(200);
        }
    }

    public void clearUnassignedPrearchiveSessions(User authUser, List<String> studyInstanceUIDs) {
        final JsonPath jsonPath = Credentials.build(authUser).given().queryParam("format", "json").get(formatRestUrl("prearchive")).
                then().assertThat().statusCode(200).and().extract().jsonPath().setRoot("ResultSet.Result");

        final List<String> deleteUrls = jsonPath.param("UIDs", studyInstanceUIDs).getList("findAll { it.tag in UIDs && it.project == 'Unassigned' }.url");

        for (String deleteUrl : deleteUrls) {
            Credentials.build(authUser).delete(formatRestUrl(deleteUrl)).then().assertThat().statusCode(200);
        }
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

    public void setSiteAnonScriptStatus(User authUser, boolean status) {
        Credentials.build(authUser).queryParam("activate", status).put(formatRestUrl("/config/edit/image/dicom/status")).then().assertThat().statusCode(200);
    }

    public void disableSiteAnonScript(User authUser) {
        setSiteAnonScriptStatus(authUser, false);
    }

    public void enableSiteAnonScript(User authUser) {
        setSiteAnonScriptStatus(authUser, true);
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

    public void addUsersToProject(User authUser, Project project) {
        for (Map.Entry<UserGroup, List<User>> userGroupEntry : project.getUsers().entrySet()) {
            for (User user : userGroupEntry.getValue()) {
                addUserToProject(authUser, user, project, userGroupEntry.getKey());
            }
        }
    }

    public void addUserToProject(User authUser, User addedUser, Project project, UserGroup userGroup) { // TODO: does this work for Custom User groups?
        addUserToGroups(authUser, addedUser, String.format("%s_%s", project, userGroup.singularName().toLowerCase()));
    }

    public void uploadResources(User authUser, List<Resource> resources) {
        for (Resource resource : resources) {
            uploadResource(authUser, resource);
        }
    }

    public void uploadResource(User authUser, Resource resource) {
        Credentials.build(authUser).given().queryParams(SerializationUtils.serializeToMap(resource)).
                put(formatXnatUrl(resource.resourceUrl(), "resources", resource.getFolder())).then().assertThat().statusCode(200);

        for (ResourceFile file : resource.getResourceFiles()) {
            if (file.getExtension() == null) {
                final File possibleFile = FileIO.getDataFile(file.getName());
                if (possibleFile != null) {
                    file.extension(new SimpleResourceFileExtension(file, possibleFile));
                } else {
                    throw new UnsupportedOperationException("ResourceFile must have extension set in order to locate file for upload.");
                }
            }

            Credentials.build(authUser).expect().statusCode(200).given().
                    queryParams(SerializationUtils.serializeToMap(file)).multiPart(file.getExtension().getJavaFile()).
                    put(resourceFileUrl(resource, file));
        }
    }

    public String resourceFilesUrl(Resource resource) {
        return formatXnatUrl(resource.resourceUrl(), "resources", resource.getFolder(), "files");
    }

    public String resourceFileUrl(Resource resource, ResourceFile file) {
        return CommonUtils.formatUrl(resourceFilesUrl(resource), file.getName());
    }

    public void validateUpload(User authUser, String fileUrl, File localFile) {
        TestNgUtils.assertBinaryFilesEqual(
                localFile,
                saveBinaryResponseToFile(Credentials.build(authUser).get(fileUrl)));
    }

    public void validateResource(User authUser, Resource resource) {
        for (ResourceFile resourceFile : resource.getResourceFiles()) {
            validateUpload(authUser, resourceFileUrl(resource, resourceFile), resourceFile.getExtension().getJavaFile());
        }
    }

    public String accessibilityRestUrl(Project project) {
        return formatRestUrl("projects", project.getId(), "accessibility");
    }

    public String accessibilityRestUrl(Project project, Accessibility accessibility) {
        return formatRestUrl("projects", project.getId(), "accessibility", accessibility.toString());
    }

    public void updateAccessibility(User authUser, Project project, Accessibility accessibility) {
        Credentials.build(authUser).expect().statusCode(200).put(accessibilityRestUrl(project, accessibility));
        project.accessibility(accessibility);
    }

    public void assertProjectAccessibility(User authUser, Project project, Accessibility accessibility) {
        Credentials.build(authUser).expect().statusCode(200).given().queryParam("format", "xml").get(accessibilityRestUrl(project)).then().assertThat().body(equalTo(accessibility.toString()));
    }

    public String projectUrl(Project project) {
        return formatRestUrl("projects", project.getId());
    }

    public void createProject(User authUser, Project project, File projectXmlFile) {
        Credentials.build(authUser).expect().statusCode(200).
                given().queryParam("format", "xml").contentType(XML).content(FileIO.readFile(projectXmlFile)).put(projectUrl(project));
    }

    public void createProject(User authUser, Project project) {
        if (project == null) {
            throw new UnsupportedOperationException("project cannot be null");
        }

        if (project.getExtension() == null) {
            project.extension(new ProjectQueryPutExtension(this, project));
        }

        project.getExtension().create(authUser);
        if (project.getPrearchiveCode() != null) setPrearchiveSetting(authUser, project, project.getPrearchiveCode());

        final List<Investigator> investigators = new ArrayList<>();
        investigators.addAll(project.getInvestigators());
        if (project.getPi() != null) investigators.add(project.getPi());
        createInvestigators(authUser, investigators);

        addUsersToProject(authUser, project);
        for (Resource resource : project.getProjectResources()) {
            resource.setProject(project);
        }
        uploadResources(authUser, project.getProjectResources());

        for (Subject subject : project.getSubjects()) {
            createSubject(authUser, subject.project(project));
        }
    }

    public void setPrearchiveSetting(User authUser, Project project, PrearchiveCode code) {
        Credentials.build(authUser).put(formatRestUrl("projects", project.getId(), "prearchive_code", Integer.toString(code.getCode()))).then().assertThat().statusCode(200);
    }

    public void createSubject(User authUser, Project project, Subject subject) {
        if (project == null) {
            throw new UnsupportedOperationException("project cannot be null");
        }
        if (subject == null) {
            throw new UnsupportedOperationException("subject cannot be null");
        }

        if (subject.getExtension() == null) {
            subject.extension(new SubjectQueryPutExtension(this, subject));
        }

        subject.getExtension().create(authUser, project);

        for (Resource resource : subject.getResources()) {
            resource.project(project).subject(subject);
        }
        uploadResources(authUser, subject.getResources());

        for (Share share : subject.getShares()) {
            shareSubject(authUser, project, subject, share);
        }

        for (SubjectAssessor assessor : subject.getExperiments()) {
            createSubjectAssessor(authUser, project, subject, assessor);
        }
    }

    public void createSubject(User authUser, Subject subject) {
        if (subject.getProject() == null) {
            throw new UnsupportedOperationException("Subject object must have Project object populated to use this shortcut method");
        }

        createSubject(authUser, subject.getProject(), subject);
    }

    public Subject createSubject(User authUser, Project project, File subjectXML) {
        final String subjectResponse = Credentials.build(authUser).expect().statusCode(200).
                given().queryParam("format", "xml").contentType(XML).content(FileIO.readFile(subjectXML)).post(formatRestUrl("projects", project.getId(), "subjects")).asString();
        return readSubject(authUser, CommonUtils.last(subjectResponse.split("/"))).project(project);
    }

    public void shareSubject(User authUser, Project sourceProject, Subject subject, Share share) {
        if (share.getDestinationProject() == null) {
            throw new UnsupportedOperationException("Destination project string cannot be null for subject sharing.");
        }

        Credentials.build(authUser).given().queryParam("label", (share.getDestinationLabel() != null) ? share.getDestinationLabel() : subject.getLabel()).
                put(CommonUtils.formatUrl(subjectUrl(sourceProject, subject), "projects", share.getDestinationProject())).then().assertThat().statusCode(200);
    }

    public void deleteSubject(User authUser, Project project, Subject subject) {
        if (project == null) {
            throw new UnsupportedOperationException("project cannot be null");
        }
        if (subject == null) {
            throw new UnsupportedOperationException("subject cannot be null");
        }

        Credentials.build(authUser).delete(subjectUrl(project, subject)).then().assertThat().statusCode(200);
    }

    public void deleteSubject(User authUser, Subject subject) {
        deleteSubject(authUser, subject.getProject(), subject);
    }

    public String subjectUrl(Project project, Subject subject) {
        return formatRestUrl("projects", project.getId(), "subjects", subject.getLabel());
    }

    public String subjectUrl(Subject subject) {
        return subjectUrl(subject.getProject(), subject);
    }

    public void createSubjectAssessor(User authUser, Project project, Subject subject, SubjectAssessor subjectAssessor) {
        if (project == null) {
            throw new UnsupportedOperationException("project cannot be null");
        }
        if (subject == null) {
            throw new UnsupportedOperationException("subject cannot be null");
        }
        if (subjectAssessor == null) {
            throw new UnsupportedOperationException("subjectAssessor cannot be null");
        }

        if (subjectAssessor.getExtension() == null) {
            subjectAssessor.extension(new SubjectAssessorQueryPutExtension(this, subjectAssessor));
        }
        subjectAssessor.getExtension().create(authUser, project, subject);

        for (Resource resource : subjectAssessor.getResources()) {
            resource.project(project).subject(subject).subjectAssessor(subjectAssessor);
        }
        uploadResources(authUser, subjectAssessor.getResources());

        for (Share share : subjectAssessor.getShares()) {
            shareSubjectAssessor(authUser, project, subject, subjectAssessor, share);
        }

        if (subjectAssessor instanceof ImagingSession) {
            final ImagingSession session = (ImagingSession)subjectAssessor;

            for (Scan scan : session.getScans()) {
                createScan(authUser, project, subject, session, scan);
            }

            for (SessionAssessor assessor : session.getAssessors()) {
                createSessionAssessor(authUser, project, subject, session, assessor);
            }
        }
    }

    public void createSubjectAssessor(User authUser, SubjectAssessor subjectAssessor) {
        createSubjectAssessor(authUser, subjectAssessor.getPrimaryProject(), subjectAssessor.getSubject(), subjectAssessor);
    }

    public void shareSubjectAssessor(User authUser, Project project, Subject subject, SubjectAssessor subjectAssessor, Share share) {
        if (share.getDestinationProject() == null) {
            throw new UnsupportedOperationException("Destination project string cannot be null for experiment sharing.");
        }

        Credentials.build(authUser).given().queryParam("label", (share.getDestinationLabel() != null) ? share.getDestinationLabel() : subjectAssessor.getLabel()).
                put(CommonUtils.formatUrl(subjectAssessorUrl(project, subject, subjectAssessor), "projects", share.getDestinationProject())).then().assertThat().statusCode(200);
    }

    public void deleteSubjectAssessor(User authUser, Project project, Subject subject, SubjectAssessor subjectAssessor) {
        if (project == null) {
            throw new UnsupportedOperationException("project cannot be null");
        }
        if (subject == null) {
            throw new UnsupportedOperationException("subject cannot be null");
        }
        if (subjectAssessor == null) {
            throw new UnsupportedOperationException("subjectAssessor cannot be null");
        }

        Credentials.build(authUser).delete(subjectAssessorUrl(project, subject, subjectAssessor)).then().assertThat().statusCode(200);
    }

    public void deleteSubjectAssessor(User authUser, SubjectAssessor subjectAssessor) {
        deleteSubjectAssessor(authUser, subjectAssessor.getPrimaryProject(), subjectAssessor.getSubject(), subjectAssessor);
    }

    public String subjectAssessorUrl(Project project, Subject subject, SubjectAssessor assessor) {
        return formatRestUrl("projects", project.getId(), "subjects", subject.getLabel(), "experiments", assessor.getLabel());
    }

    public String subjectAssessorUrl(SubjectAssessor assessor) {
        return subjectAssessorUrl(assessor.getPrimaryProject() != null ? assessor.getPrimaryProject() : assessor.getSubject().getProject(), assessor.getSubject(), assessor);
    }

    public String sessionScansUrl(Project project, Subject subject, ImagingSession session) {
        return formatRestUrl("projects", project.getId(), "subjects", subject.getLabel(), "experiments", session.getLabel(), "scans");
    }

    public String sessionScansUrl(ImagingSession session) {
        return sessionScansUrl(session.getPrimaryProject(), session.getSubject(), session);
    }

    public void createScan(User authUser, Project project, Subject subject, ImagingSession session, Scan scan) {
        Credentials.build(authUser).given().expect().statusCode(200).given().queryParams(SerializationUtils.serializeToMap(scan)).
                put(formatRestUrl("projects", project.getId(), "subjects", subject.getLabel(), "experiments", session.getLabel(), "scans", scan.getId()));

        for (Resource resource : scan.getScanResources()) {
            resource.project(project).subject(subject).subjectAssessor(session).scan(scan);
        }
        uploadResources(authUser, scan.getScanResources());
    }

    public String scanUrl(Project project, Subject subject, ImagingSession session, Scan scan) {
        return CommonUtils.formatUrl(sessionScansUrl(project, subject, session), scan.getId());
    }

    public String scanUrl(Scan scan) {
        return scanUrl(scan.getSession().getPrimaryProject(), scan.getSession().getSubject(), scan.getSession(), scan);
    }

    public String assessorsUrl(Project project, Subject subject, ImagingSession session) {
        return formatRestUrl("projects", project.getId(), "subjects", subject.getLabel(), "experiments", session.getLabel(), "assessors");
    }

    public void createSessionAssessor(User authUser, Project project, Subject subject, ImagingSession session, SessionAssessor assessor) {
        if (project == null) {
            throw new UnsupportedOperationException("project cannot be null");
        }
        if (subject == null) {
            throw new UnsupportedOperationException("subject cannot be null");
        }
        if (session == null) {
            throw new UnsupportedOperationException("session cannot be null");
        }
        if (assessor == null) {
            throw new UnsupportedOperationException("assessor cannot be null");
        }

        if (assessor.getExtension() == null) {
            assessor.extension(new SessionAssessorQueryPutExtension(this, assessor));
        }

        assessor.getExtension().create(authUser, project, subject, session);

        for (Resource resource : assessor.getResources()) {
            resource.project(project).subject(subject).subjectAssessor(session).sessionAssessor(assessor);
        }
        uploadResources(authUser, assessor.getResources());
    }

    public void createSessionAssessor(User authUser, SessionAssessor assessor) {
        createSessionAssessor(authUser, assessor.getPrimaryProject(), assessor.getSubject(), assessor.getParentSession(), assessor);
    }

    public void deleteSessionAssessor(User authUser, Project project, Subject subject, ImagingSession session, SessionAssessor sessionAssessor) {
        if (project == null) {
            throw new UnsupportedOperationException("project cannot be null");
        }
        if (subject == null) {
            throw new UnsupportedOperationException("subject cannot be null");
        }
        if (session == null) {
            throw new UnsupportedOperationException("session cannot be null");
        }
        if (sessionAssessor == null) {
            throw new UnsupportedOperationException("sessionAssessor cannot be null");
        }

        Credentials.build(authUser).delete(sessionAssessorUrl(project, subject, session, sessionAssessor)).then().assertThat().statusCode(200);
    }

    public void deleteSessionAssessor(User authUser, SessionAssessor sessionAssessor) {
        deleteSessionAssessor(authUser, sessionAssessor.getPrimaryProject(), sessionAssessor.getSubject(), sessionAssessor.getParentSession(), sessionAssessor);
    }

    public String sessionAssessorUrl(Project project, Subject subject, ImagingSession session, SessionAssessor sessionAssessor) {
        return formatRestUrl("projects", project.getId(), "subjects", subject.getLabel(), "experiments", session.getLabel(), "assessors", sessionAssessor.getLabel());
    }

    public String sessionAssessorUrl(SessionAssessor assessor) {
        return sessionAssessorUrl(assessor.getPrimaryProject(), assessor.getSubject(), assessor.getParentSession(), assessor);
    }

    public void deleteProject(User authUser, Project project) {
        Credentials.build(authUser).expect().statusCode(200).given().queryParam("removeFiles", true).delete(projectUrl(project));
    }

    public void deleteProjectSilently(User authUser, Project project) {
        try {
            deleteProject(authUser, project);
        } catch (Exception | Error ignored) {}
    }

    public String assessorsByAccessionNumber(ImagingSession session) {
        if (session.getAccessionNumber() == null) {
            throw new UnsupportedOperationException("Method requires session object to have accessionNumber populated");
        }

        return formatRestUrl("experiments", session.getAccessionNumber(), "assessors");
    }

    public String assessorByAccessionNumber(ImagingSession session, SessionAssessor assessor) {
        if (session.getAccessionNumber() == null) {
            throw new UnsupportedOperationException("Method requires session object to have accessionNumber populated.");
        }
        if (assessor.getAccessionNumber() == null) {
            throw new UnsupportedOperationException("Method requires assessor object to have accessionNumber populated.");
        }

        return formatRestUrl("experiments", session.getAccessionNumber(), "assessors", assessor.getAccessionNumber());
    }

}
