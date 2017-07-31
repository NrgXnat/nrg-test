package org.nrg.testing.xnat.extensions;

import org.nrg.testing.auth.Credentials;
import org.nrg.testing.xnat.rest.SerializationUtils;
import org.nrg.testing.xnat.rest.XnatRestDriver;
import org.nrg.xnat.pojo.Project;
import org.nrg.xnat.pojo.Subject;
import org.nrg.xnat.pojo.experiments.ImagingSession;
import org.nrg.xnat.pojo.experiments.SessionAssessor;
import org.nrg.xnat.pojo.extensions.SessionAssessorExtension;
import org.nrg.xnat.pojo.users.User;

public class SessionAssessorQueryPutExtension extends SessionAssessorExtension {

    private XnatRestDriver restDriver;

    public SessionAssessorQueryPutExtension(XnatRestDriver restDriver, SessionAssessor sessionAssessor) {
        super(sessionAssessor);
        this.restDriver = restDriver;
    }

    @Override
    public void create(User authUser, Project project, Subject subject, ImagingSession session) {
        if (getParentObject().getDataType() == null) {
            throw new UnsupportedOperationException("SessionAssessor must have xsiType to pass to XNAT for this method");
        }

        getParentObject().accessionNumber(
                Credentials.build(authUser).expect().statusCode(201).given().queryParams(SerializationUtils.serializeToMap(getParentObject())).
                        put(restDriver.sessionAssessorUrl(project, subject, session, (SessionAssessor) getParentObject())).asString()
        );
    }


}
