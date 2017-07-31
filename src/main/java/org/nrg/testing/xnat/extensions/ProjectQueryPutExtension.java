package org.nrg.testing.xnat.extensions;

import org.nrg.testing.auth.Credentials;
import org.nrg.testing.xnat.rest.SerializationUtils;
import org.nrg.testing.xnat.rest.XnatRestDriver;
import org.nrg.xnat.pojo.Project;
import org.nrg.xnat.pojo.extensions.ProjectExtension;
import org.nrg.xnat.pojo.users.User;

public class ProjectQueryPutExtension extends ProjectExtension {

    private XnatRestDriver restDriver;

    public ProjectQueryPutExtension(XnatRestDriver restDriver, Project project) {
        super(project);
        this.restDriver = restDriver;
    }

    @Override
    public void create(User authUser) {
        Credentials.build(authUser).expect().statusCode(200).given().queryParameters(SerializationUtils.serializeToMap(getParentObject())).put(restDriver.projectUrl(getParentObject()));
    }

}
