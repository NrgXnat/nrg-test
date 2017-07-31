package org.nrg.testing.xnat.extensions;

import com.jayway.restassured.http.ContentType;
import org.nrg.testing.CommonUtils;
import org.nrg.testing.auth.Credentials;
import org.nrg.testing.file.FileIO;
import org.nrg.testing.xnat.rest.XnatRestDriver;
import org.nrg.xnat.pojo.Project;
import org.nrg.xnat.pojo.Subject;
import org.nrg.xnat.pojo.experiments.SubjectAssessor;
import org.nrg.xnat.pojo.extensions.SubjectAssessorExtension;
import org.nrg.xnat.pojo.users.User;

import java.io.File;

public class SubjectAssessorXMLExtension extends SubjectAssessorExtension {

    private File assessorXML;
    private XnatRestDriver restDriver;

    public SubjectAssessorXMLExtension(XnatRestDriver restDriver, SubjectAssessor assessor, File assessorXML) {
        super(assessor);
        this.assessorXML = assessorXML;
        this.restDriver = restDriver;
    }

    public SubjectAssessorXMLExtension(XnatRestDriver restDriver, File assessorXML) {
        this(restDriver, null, assessorXML);
    }

    @Override
    public void create(User authUser, Project project, Subject subject) {
        getParentObject().accessionNumber(
                CommonUtils.last(
                        Credentials.build(authUser).given().contentType(ContentType.XML).queryParam("format", "xml").body(FileIO.readFile(assessorXML)).post(restDriver.formatRestUrl("projects", project.getId(), "subjects", subject.getLabel(), "experiments")).asString().split("/")
                )
        );
        final SubjectAssessor createdAssessor = (SubjectAssessor) restDriver.readExperiment(authUser, getParentObject().getAccessionNumber(), getParentObject().getClass());
        subject.removeExperiment(createdAssessor);
        getParentObject().label(createdAssessor.getLabel());
    }

}
