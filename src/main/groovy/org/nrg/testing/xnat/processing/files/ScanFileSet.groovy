package org.nrg.testing.xnat.processing.files

import org.nrg.testing.xnat.rest.XnatRestDriver
import org.nrg.xnat.pogo.experiments.ImagingSession

class ScanFileSet extends ProcessingFileSets {

    String scanId

    @Override
    String findBaseUrlForResources(XnatRestDriver driver, ImagingSession session) {
        "/data/experiments/${session.accessionNumber}/scans/${scanId}"
    }

    @Override
    int numIntermediateLocalResourceFolders() {
        3
    }

}
