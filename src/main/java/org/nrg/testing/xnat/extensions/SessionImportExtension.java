package org.nrg.testing.xnat.extensions;

import org.nrg.testing.xnat.rest.XnatRestDriver;
import org.nrg.xnat.pojo.Project;
import org.nrg.xnat.pojo.Subject;
import org.nrg.xnat.pojo.experiments.ImagingSession;
import org.nrg.xnat.pojo.extensions.SubjectAssessorExtension;
import org.nrg.xnat.pojo.users.User;

import java.io.File;

public class SessionImportExtension extends SubjectAssessorExtension {

    private XnatRestDriver restDriver;
    private File sessionZip;

    public SessionImportExtension(XnatRestDriver restDriver, ImagingSession session, File sessionZip) {
        super(session);
        this.restDriver = restDriver;
        this.sessionZip = sessionZip;
    }

    @Override
    public void create(User authUser, Project project, Subject subject) {
        restDriver.uploadToSessionZipImporter(authUser, sessionZip, project, subject, (ImagingSession)getParentObject());
    }

}
