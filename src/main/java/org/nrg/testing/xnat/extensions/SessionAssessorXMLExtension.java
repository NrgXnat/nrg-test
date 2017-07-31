package org.nrg.testing.xnat.extensions;

import com.jayway.restassured.http.ContentType;
import org.nrg.testing.CommonUtils;
import org.nrg.testing.auth.Credentials;
import org.nrg.testing.file.FileIO;
import org.nrg.testing.xnat.rest.XnatRestDriver;
import org.nrg.xnat.pojo.Project;
import org.nrg.xnat.pojo.Subject;
import org.nrg.xnat.pojo.experiments.ImagingSession;
import org.nrg.xnat.pojo.experiments.SessionAssessor;
import org.nrg.xnat.pojo.extensions.SessionAssessorExtension;
import org.nrg.xnat.pojo.users.User;

import java.io.File;

public class SessionAssessorXMLExtension extends SessionAssessorExtension {

    private XnatRestDriver restDriver;
    private File assessorXML;

    public SessionAssessorXMLExtension(XnatRestDriver restDriver, SessionAssessor assessor, File assessorXML) {
        super(assessor);
        this.restDriver = restDriver;
        this.assessorXML = assessorXML;
    }

    public SessionAssessorXMLExtension(XnatRestDriver restDriver, File assessorXML) {
        this(restDriver, null, assessorXML);
    }

    @Override
    public void create(User authUser, Project project, Subject subject, ImagingSession session) {
        getParentObject().accessionNumber(
                CommonUtils.last(
                        Credentials.build(authUser).given().contentType(ContentType.XML).queryParam("format", "xml").body(FileIO.readFile(assessorXML)).post(restDriver.assessorsUrl(project, subject, session)).asString().split("/")
                )
        );
        final SessionAssessor createdAssessor = (SessionAssessor) restDriver.readExperiment(authUser, getParentObject().getAccessionNumber(), getParentObject().getClass());
        session.removeAssessor(createdAssessor);
        getParentObject().label(createdAssessor.getLabel());
    }

}
