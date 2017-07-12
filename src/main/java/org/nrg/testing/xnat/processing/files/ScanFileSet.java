/*
 * org.nrg.selenium.pipeline.pipelineFileLocations.ScanFileSet
 * XNAT http://www.xnat.org
 * Copyright (c) 2015, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified: 10/30/15 6:17 PM
 */

package org.nrg.testing.xnat.processing.files;

import org.nrg.testing.xnat.rest.XnatRestDriver;

public class ScanFileSet extends ProcessingFileSet {

    String scanId;

    public ScanFileSet(String scanId, String csvFileSetName, String session) {
        this.scanId = scanId;
        this.csvFileSetName = csvFileSetName;
        this.session = session;
    }

    @Override
    public String restSubcall(XnatRestDriver xnatDriver) {
        return String.format("scans/%s/files", scanId);
    }
}
