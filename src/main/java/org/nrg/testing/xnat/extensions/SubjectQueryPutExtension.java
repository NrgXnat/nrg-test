package org.nrg.testing.xnat.extensions;

import org.nrg.testing.auth.Credentials;
import org.nrg.testing.xnat.rest.SerializationUtils;
import org.nrg.testing.xnat.rest.XnatRestDriver;
import org.nrg.xnat.pojo.Project;
import org.nrg.xnat.pojo.Subject;
import org.nrg.xnat.pojo.extensions.SubjectExtension;
import org.nrg.xnat.pojo.users.User;

public class SubjectQueryPutExtension extends SubjectExtension {

    private XnatRestDriver restDriver;

    public SubjectQueryPutExtension(XnatRestDriver restDriver, Subject subject) {
        super(subject);
        this.restDriver = restDriver;
    }

    @Override
    public void create(User authUser, Project project) {
        Credentials.build(authUser).expect().statusCode(201).given().queryParams(SerializationUtils.serializeToMap(getParentObject())).
                put(restDriver.formatRestUrl("projects", project.getId(), "subjects", getParentObject().getLabel()));
    }

}
