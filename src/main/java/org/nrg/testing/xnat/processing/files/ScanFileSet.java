package org.nrg.testing.xnat.processing.files;

import org.nrg.testing.xnat.rest.XnatRestDriver;
import org.nrg.xnat.pogo.experiments.ImagingSession;

public class ScanFileSet extends ProcessingFileSets {

    String scanId;

    public String getScanId() {
        return scanId;
    }

    public void setScanId(String scanId) {
        this.scanId = scanId;
    }

    @Override
    public String findBaseUrlForResources(XnatRestDriver driver, ImagingSession session) {
        return String.format("/data/experiments/%s/scans/%s", session.getAccessionNumber(), scanId);
    }

    @Override
    public int numIntermediateLocalResourceFolders() {
        return 3;
    }

}
