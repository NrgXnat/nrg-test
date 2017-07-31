package org.nrg.testing.xnat.extensions;

import org.nrg.testing.xnat.rest.XnatRestDriver;
import org.nrg.xnat.pojo.Project;
import org.nrg.xnat.pojo.Subject;
import org.nrg.xnat.pojo.extensions.SubjectExtension;
import org.nrg.xnat.pojo.users.User;

import java.io.File;

public class SubjectXMLPutExtension extends SubjectExtension {

    private File xmlFile;
    private XnatRestDriver restDriver;

    public SubjectXMLPutExtension(XnatRestDriver restDriver, Subject subject, File file) {
        super(subject);
        xmlFile = file;
        this.restDriver = restDriver;
    }

    public SubjectXMLPutExtension(XnatRestDriver restDriver, File file) {
        this(restDriver, null, file);
    }

    @Override
    public void create(User authUser, Project project) {
        final Subject createdSubject = restDriver.createSubject(authUser, project, xmlFile);
        project.removeSubject(createdSubject);
        getParentObject().accessionNumber(createdSubject.getAccessionNumber()).label(createdSubject.getLabel());
    }

}
