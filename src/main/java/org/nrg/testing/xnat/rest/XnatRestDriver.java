package org.nrg.testing.xnat.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.internal.RestAssuredResponseImpl;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.path.json.exception.JsonPathException;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSender;
import com.jayway.restassured.specification.RequestSpecification;
import org.apache.commons.io.FileUtils;
import org.apache.http.entity.ContentType;
import org.apache.log4j.Logger;
import org.nrg.jira.testing_components.TestStatus;
import org.nrg.testing.CommonUtils;
import org.nrg.testing.TestController;
import org.nrg.testing.auth.Credentials;
import org.nrg.testing.enums.TestData;
import org.nrg.testing.util.RandomHelper;
import org.nrg.testing.util.TestNgUtils;
import org.nrg.testing.xnat.conf.Settings;
import org.nrg.testing.xnat.conf.XnatConfig;
import org.nrg.testing.xnat.versions.XnatVersion;
import org.nrg.testing.xnat.versions.XnatVersionList;
import org.nrg.xnat.enums.Accessibility;
import org.nrg.xnat.enums.PrearchiveCode;
import org.nrg.xnat.interfaces.XnatInterface;
import org.nrg.xnat.pogo.*;
import org.nrg.xnat.pogo.experiments.*;
import org.nrg.xnat.pogo.extensions.project.ProjectXMLPutExtension;
import org.nrg.xnat.pogo.extensions.subject.SubjectExtension;
import org.nrg.xnat.pogo.extensions.subject.SubjectXMLPutExtension;
import org.nrg.xnat.pogo.resources.Resource;
import org.nrg.xnat.pogo.resources.ResourceFile;
import org.nrg.xnat.pogo.users.User;
import org.nrg.xnat.pogo.users.UserGroup;
import org.nrg.xnat.rest.XnatAliasToken;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.*;

import static com.jayway.restassured.http.ContentType.*;
import static org.hamcrest.CoreMatchers.equalTo;

public abstract class XnatRestDriver {

    protected XnatConfig xnatConfig;
    protected TestController testController;
    protected static final Logger LOGGER = Logger.getLogger(XnatRestDriver.class);
    protected static final Map<User, XnatInterface> xnatInterfaceMap = new HashMap<>();
    public static final ObjectMapper XNAT_REST_MAPPER = XnatInterface.XNAT_REST_MAPPER;

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

    public XnatInterface interfaceFor(User user) {
        final XnatInterface interf = xnatInterfaceMap.get(user);
        if (interf != null) {
            return interf;
        } else {
            final XnatInterface newInterface = XnatInterface.authenticate(xnatConfig.getXnatUrl(), user, true);
            xnatInterfaceMap.put(user, newInterface);
            return newInterface;
        }
    }

    public XnatInterface mainInterface() {
        return interfaceFor(mainUser());
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
        return mainInterface().issueAliasTokenUrl();
    }

    public XnatAliasToken generateAliasToken(User user) {
        return interfaceFor(user).generateAliasToken();
    }
    
    public String projectExperimentsUrl(Project project) {
        return mainInterface().projectExperimentsUrl(project);
    }

    public String getAccessionNumber(User authUser, SubjectAssessor subjectAssessor) {
        return interfaceFor(authUser).getAccessionNumber(subjectAssessor);
    }

    public String getAccessionNumber(ImagingSession session) {
        return getAccessionNumber(mainUser(), session);
    }

    public Subject readSubject(User authUser, String accessionNumber) {
        return interfaceFor(authUser).readSubject(accessionNumber);
    }

    public <T extends Experiment> T readExperiment(User authUser, String accessionNumber, Class<T> tClass) {
        return interfaceFor(authUser).readExperiment(accessionNumber, tClass);
    }

    public void waitForAutoRun(User authUser, int maximumTime, ImagingSession session) {
        interfaceFor(authUser).waitForAutoRun(session, maximumTime);
    }

    public void waitForAutoRun(ImagingSession session) {
        waitForAutoRun(mainUser(), 60, session);
    }

    public String getBuildInfo() {
        try {
            return mainInterface().getBuildInfo();
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

    public void clearProject(User authUser, Project project) {
        interfaceFor(authUser).deleteAllProjectData(project);
    }

    public void uploadToSessionZipImporter(User authUser, File sessionZip, Project project, Subject subject, ImagingSession session) {
        interfaceFor(authUser).uploadToSessionZipImporter(sessionZip, project, subject, session);
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
        return mainInterface().userSessionsRestUrl(user);
    }

    public void expireAllActiveSessions(User authUser, User targetUser) {
        interfaceFor(authUser).expireAllActiveSessions(targetUser);
    }

    public int getNumberActiveSessions(User user) {
        return interfaceFor(user).getNumberActiveSessions();
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
            createUser(xnatConfig.getMainUser().email(Settings.EMAIL));
        }

        if (allUsers.contains(mainAdminUser().getUsername())) {
            verifyUser(adminUser(), mainAdminUser());
            enableUser(adminUser(), mainAdminUser());
            makeUserAdmin(adminUser(), mainAdminUser());
        } else {
            createUser(xnatConfig.getMainAdminUser().email(Settings.EMAIL));
        }
    }

    public void createUser(User user) {
        interfaceFor(adminUser()).createUser(user);
    }

    public void assignUserToRoles(User authUser, User targetUser, String... roles) {
        interfaceFor(authUser).assignUserToRoles(targetUser, roles);
    }

    public void addUserToGroups(User authUser, User targetUser, String... groups) {
        interfaceFor(authUser).addUserToGroups(targetUser, groups);
    }

    public void verifyUser(User authUser, User targetUser) {
        interfaceFor(authUser).verifyUser(targetUser);
    }

    public void enableUser(User authUser, User targetUser) {
        interfaceFor(authUser).enableUser(targetUser);
    }

    public void makeUserAdmin(User authUser, User targetUser) {
        interfaceFor(authUser).makeUserAdmin(targetUser);
    }

    public String siteAnonScriptUrl() {
        return mainInterface().siteAnonScriptUrl();
    }

    public AnonScript getSiteAnonScript(User authUser) {
        return interfaceFor(authUser).readSiteAnonScript();
    }

    public void setSiteAnonScriptStatus(User authUser, boolean status) {
        interfaceFor(authUser).setSiteAnonScriptStatus(status);
    }

    public void disableSiteAnonScript(User authUser) {
        setSiteAnonScriptStatus(authUser, false);
    }

    public void enableSiteAnonScript(User authUser) {
        setSiteAnonScriptStatus(authUser, true);
    }

    public String projectAnonScriptUrl(Project project) {
        return mainInterface().projectAnonScriptUrl(project);
    }

    public AnonScript getProjectAnonScript(User authUser, Project project) {
        return interfaceFor(authUser).readProjectAnonScript(project);
    }

    public void setProjectAnonScript(User authUser, Project project, AnonScript script) {
        interfaceFor(authUser).setProjectAnonScript(project, script);
    }

    public void setProjectAnonScriptStatus(User authUser, Project project, boolean status) {
        interfaceFor(authUser).setProjectAnonScriptStatus(project, status);
    }

    public void disableProjectAnonScript(User authUser, Project project) {
        setProjectAnonScriptStatus(authUser, project, false);
    }

    public void enableProjectAnonScript(User authUser, Project project) {
        setProjectAnonScriptStatus(authUser, project, true);
    }

    public void createInvestigators(User authUser, List<Investigator> investigators) {
        interfaceFor(authUser).createInvestigators(investigators);
    }

    public void createInvestigator(User authUser, Investigator investigator) {
        interfaceFor(authUser).createInvestigator(investigator);
    }

    public void addUserToProject(User authUser, User addedUser, Project project, UserGroup userGroup) {
        interfaceFor(authUser).addUserToProject(addedUser, project, userGroup);
    }

    public void uploadResources(User authUser, List<Resource> resources) {
        for (Resource resource : resources) {
            uploadResource(authUser, resource);
        }
    }

    public void uploadResource(User authUser, Resource resource) {
        interfaceFor(authUser).uploadResource(resource);
    }

    public void deleteResource(User authUser, Resource resource) {
        interfaceFor(authUser).deleteResource(resource);
    }

    public String resourceFilesUrl(Resource resource) {
        return mainInterface().resourceFilesUrl(resource);
    }

    public String resourceFileUrl(Resource resource, ResourceFile file) {
        return mainInterface().resourceFileUrl(resource, file);
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

    public Project readProject(User authUser, String projectId) {
        return interfaceFor(authUser).readProject(projectId);
    }

    public List<Subject> readSubjects(User authUser, Project project) {
        return interfaceFor(authUser).readSubjects(project);
    }

    public List<SubjectAssessor> readSubjectAssesssors(User authUser, Project project, Subject subject) {
        return interfaceFor(authUser).readSubjectAssessors(project, subject);
    }

    public List<Scan> readScans(User authUser, Project project, Subject subject, ImagingSession session) {
        return interfaceFor(authUser).readScans(project, subject, session);
    }

    public List<SessionAssessor> readSessionAssessors(User authUser, Project project, Subject subject, ImagingSession session) {
        return interfaceFor(authUser).readSessionAssessors(project, subject, session);
    }

    public Subject findSubject(Project project, String label) {
        return mainInterface().findSubject(project, label);
    }

    public SubjectAssessor findSubjectAssessor(Subject subject, String label) {
        return mainInterface().findSubjectAssessor(subject, label);
    }

    public SessionAssessor findSessionAssessor(ImagingSession session, String label) {
        return mainInterface().findSessionAssessor(session, label);
    }

    public Resource findResource(List<Resource> resources, String label) {
        return mainInterface().findResource(resources, label);
    }

    public List<Scan> filterScansByType(List<Scan> scans, String type) {
        return mainInterface().filterScansByType(scans, type);
    }

    public List<Scan> filterScansByType(List<Scan> scans, List<String> acceptableTypes) {
        return mainInterface().filterScansByType(scans, acceptableTypes);
    }

    public Scan findScan(ImagingSession session, String scanId) {
        return mainInterface().findScan(session, scanId);
    }

    public String accessibilityRestUrl(Project project) {
        return mainInterface().accessibilityRestUrl(project);
    }

    public String accessibilityRestUrl(Project project, Accessibility accessibility) {
        return mainInterface().accessibilityRestUrl(project, accessibility);
    }

    public void updateAccessibility(User authUser, Project project, Accessibility accessibility) {
        interfaceFor(authUser).updateAccessibility(project, accessibility);
    }

    public void assertProjectAccessibility(User authUser, Project project, Accessibility accessibility) {
        Credentials.build(authUser).expect().statusCode(200).given().queryParam("format", "xml").get(accessibilityRestUrl(project)).then().assertThat().body(equalTo(accessibility.toString()));
    }

    public String projectUrl(Project project) {
        return mainInterface().projectUrl(project);
    }

    public void createProject(User authUser, Project project, File projectXmlFile) {
        new ProjectXMLPutExtension(interfaceFor(authUser), project, projectXmlFile).create();
    }

    public void createProject(User authUser, Project project) {
        interfaceFor(authUser).createProject(project);
    }

    public void setPrearchiveSetting(User authUser, Project project, PrearchiveCode code) {
        interfaceFor(authUser).setPrearchiveSetting(project, code);
    }

    public void createSubject(User authUser, Project project, Subject subject) {
        interfaceFor(authUser).createSubject(project, subject);
    }

    public void createSubject(User authUser, Subject subject) {
        interfaceFor(authUser).createSubject(subject);
    }

    public Subject createSubject(User authUser, Project project, File subjectXML) {
        final SubjectExtension extension = new SubjectXMLPutExtension(interfaceFor(authUser), subjectXML);
        extension.create(project);
        return extension.getParentObject();
    }

    public void shareSubject(User authUser, Project sourceProject, Subject subject, Share share) {
        interfaceFor(authUser).shareSubject(sourceProject, subject, share);
    }

    public void deleteSubject(User authUser, Project project, Subject subject) {
        interfaceFor(authUser).deleteSubject(project, subject);
    }

    public void deleteSubject(User authUser, Subject subject) {
        interfaceFor(authUser).deleteSubject(subject);
    }

    public String subjectUrl(Project project, Subject subject) {
        return mainInterface().subjectUrl(project, subject);
    }

    public String subjectUrl(Subject subject) {
        return mainInterface().subjectUrl(subject);
    }

    public void createSubjectAssessor(User authUser, Project project, Subject subject, SubjectAssessor subjectAssessor) {
        interfaceFor(authUser).createSubjectAssessor(project, subject, subjectAssessor);
    }

    public void createSubjectAssessor(User authUser, SubjectAssessor subjectAssessor) {
        interfaceFor(authUser).createSubjectAssessor(subjectAssessor);
    }

    public void shareSubjectAssessor(User authUser, Project project, Subject subject, SubjectAssessor subjectAssessor, Share share) {
        interfaceFor(authUser).shareSubjectAssessor(project, subject, subjectAssessor, share);
    }

    public void deleteSubjectAssessor(User authUser, Project project, Subject subject, SubjectAssessor subjectAssessor) {
        interfaceFor(authUser).deleteSubjectAssessor(project, subject, subjectAssessor);
    }

    public void deleteSubjectAssessor(User authUser, SubjectAssessor subjectAssessor) {
        interfaceFor(authUser).deleteSubjectAssessor(subjectAssessor);
    }

    public String subjectAssessorUrl(Project project, Subject subject, SubjectAssessor assessor) {
        return mainInterface().subjectAssessorUrl(project, subject, assessor);
    }

    public String subjectAssessorUrl(SubjectAssessor assessor) {
        return mainInterface().subjectAssessorUrl(assessor);
    }

    public String sessionScansUrl(Project project, Subject subject, ImagingSession session) {
        return mainInterface().sessionScansUrl(project, subject, session);
    }

    public String sessionScansUrl(ImagingSession session) {
        return mainInterface().sessionScansUrl(session);
    }

    public void createScan(User authUser, Project project, Subject subject, ImagingSession session, Scan scan) {
        interfaceFor(authUser).createScan(project, subject, session, scan);
    }

    public void deleteScan(User authUser, Project project, Subject subject, ImagingSession session, Scan scan) {
        interfaceFor(authUser).deleteScan(project, subject, session, scan);
    }

    public void deleteScan(User authUser, Scan scan) {
        interfaceFor(authUser).deleteScan(scan);
    }

    public void updateScan(User authUser, Project project, Subject subject, ImagingSession session, Scan scan) {
        interfaceFor(authUser).updateScan(project, subject, session, scan);
    }

    public void updateScan(User authUser, Scan scan) {
        interfaceFor(authUser).updateScan(scan);
    }

    public String scanUrl(Project project, Subject subject, ImagingSession session, Scan scan) {
        return mainInterface().scanUrl(project, subject, session, scan);
    }

    public String scanUrl(Scan scan) {
        return mainInterface().scanUrl(scan);
    }

    public String assessorsUrl(Project project, Subject subject, ImagingSession session) {
        return mainInterface().assessorsUrl(project, subject, session);
    }

    public void createSessionAssessor(User authUser, Project project, Subject subject, ImagingSession session, SessionAssessor assessor) {
        interfaceFor(authUser).createSessionAssessor(project, subject, session, assessor);
    }

    public void createSessionAssessor(User authUser, SessionAssessor assessor) {
        interfaceFor(authUser).createSessionAssessor(assessor);
    }

    public void deleteSessionAssessor(User authUser, Project project, Subject subject, ImagingSession session, SessionAssessor sessionAssessor) {
        interfaceFor(authUser).deleteSessionAssessor(project, subject, session, sessionAssessor);
    }

    public void deleteSessionAssessor(User authUser, SessionAssessor sessionAssessor) {
        interfaceFor(authUser).deleteSessionAssessor(sessionAssessor);
    }

    public String sessionAssessorUrl(Project project, Subject subject, ImagingSession session, SessionAssessor sessionAssessor) {
        return mainInterface().sessionAssessorUrl(project, subject, session, sessionAssessor);
    }

    public String sessionAssessorUrl(SessionAssessor assessor) {
        return mainInterface().sessionAssessorUrl(assessor);
    }

    public void deleteProject(User authUser, Project project) {
        interfaceFor(authUser).deleteProject(project);
    }

    public void deleteProjectSilently(User authUser, Project project) {
        try {
            deleteProject(authUser, project);
        } catch (Exception | Error ignored) {}
    }

    public String assessorsByAccessionNumber(ImagingSession session) {
        return mainInterface().assessorsUrlByAccessionNumber(session);
    }

    public String assessorByAccessionNumber(ImagingSession session, SessionAssessor assessor) {
        return mainInterface().assessorUrlByAccessionNumber(session, assessor);
    }

}
