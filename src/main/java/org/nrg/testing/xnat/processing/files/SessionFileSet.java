package org.nrg.testing.xnat.processing.files;

import org.nrg.testing.xnat.rest.XnatRestDriver;
import org.nrg.xnat.pogo.experiments.ImagingSession;

public class SessionFileSet extends ProcessingFileSets {

    @Override
    public String findBaseUrlForResources(XnatRestDriver driver, ImagingSession session) {
        return "data/experiments/" + session.getAccessionNumber();
    }

    @Override
    public int numIntermediateLocalResourceFolders() {
        return 1;
    }

}
