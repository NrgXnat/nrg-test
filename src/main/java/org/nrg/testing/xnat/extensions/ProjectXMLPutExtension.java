package org.nrg.testing.xnat.extensions;

import org.nrg.testing.xnat.rest.XnatRestDriver;
import org.nrg.xnat.pojo.Project;
import org.nrg.xnat.pojo.extensions.ProjectExtension;
import org.nrg.xnat.pojo.users.User;

import java.io.File;

public class ProjectXMLPutExtension extends ProjectExtension {

    private File xmlFile;
    private XnatRestDriver restDriver;

    public ProjectXMLPutExtension(XnatRestDriver restDriver, Project project, File file) {
        super(project);
        xmlFile = file;
        this.restDriver = restDriver;
    }

    public ProjectXMLPutExtension(XnatRestDriver restDriver, File file) {
        this(restDriver, null, file);
    }

    @Override
    public void create(User authUser) {
        restDriver.createProject(authUser, getParentObject(), xmlFile);
    }

}
