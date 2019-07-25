package org.nrg.testing.xnat.processing.files

import org.nrg.testing.xnat.rest.XnatRestDriver
import org.nrg.xnat.pogo.experiments.ImagingSession

class SessionFileSet extends ProcessingFileSets {

    @Override
    String findBaseUrlForResources(XnatRestDriver driver, ImagingSession session) {
        "data/experiments/${session.accessionNumber}"
    }

    @Override
    int numIntermediateLocalResourceFolders() {
        1
    }

}
