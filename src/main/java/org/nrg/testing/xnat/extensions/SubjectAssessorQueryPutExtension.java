package org.nrg.testing.xnat.extensions;

import org.nrg.testing.auth.Credentials;
import org.nrg.testing.xnat.rest.SerializationUtils;
import org.nrg.testing.xnat.rest.XnatRestDriver;
import org.nrg.xnat.pojo.Project;
import org.nrg.xnat.pojo.Subject;
import org.nrg.xnat.pojo.experiments.SubjectAssessor;
import org.nrg.xnat.pojo.extensions.SubjectAssessorExtension;
import org.nrg.xnat.pojo.users.User;

public class SubjectAssessorQueryPutExtension extends SubjectAssessorExtension {

    private XnatRestDriver restDriver;

    public SubjectAssessorQueryPutExtension(XnatRestDriver restDriver, SubjectAssessor subjectAssessor) {
        super(subjectAssessor);
        this.restDriver = restDriver;
    }

    @Override
    public void create(User authUser, Project project, Subject subject) {
        if (getParentObject().getDataType() == null) {
            throw new UnsupportedOperationException("SubjectAssessor must have xsiType to pass to XNAT for this method");
        }

        getParentObject().accessionNumber(
                Credentials.build(authUser).expect().statusCode(201).given().queryParams(SerializationUtils.serializeToMap(getParentObject())).
                        put(restDriver.formatRestUrl("projects", project.getId(), "subjects", subject.getLabel(), "experiments", getParentObject().getLabel())).asString()
        );
    }

}
