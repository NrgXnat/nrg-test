package org.nrg.testing.xnat.processing.files

import org.nrg.testing.xnat.rest.XnatRestDriver
import org.nrg.xnat.pogo.experiments.ImagingSession

class AssessorFileSet extends ProcessingFileSets {

    String xsiType

    @Override
    String findBaseUrlForResources(XnatRestDriver driver, ImagingSession session) {
        "/data/experiments/${session.accessionNumber}/assessors/${xsiType}"
    }

    @Override
    int numIntermediateLocalResourceFolders() {
        2
    }

}
