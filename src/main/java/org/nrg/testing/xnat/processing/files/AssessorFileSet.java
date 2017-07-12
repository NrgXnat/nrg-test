/*
 * org.nrg.selenium.pipeline.pipelineFileLocations.AssessorFileLocation
 * XNAT http://www.xnat.org
 * Copyright (c) 2015, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified: 10/30/15 5:43 PM
 */

package org.nrg.testing.xnat.processing.files;

import org.nrg.testing.xnat.rest.XnatRestDriver;

public class AssessorFileSet extends ProcessingFileSet {

    private String assessor;

    public AssessorFileSet(String assessor, String csvFileSetName, String session) {
        this.assessor = assessor;
        this.csvFileSetName = csvFileSetName;
        this.session = session;
    }

    @Override
    public String restSubcall(XnatRestDriver xnatDriver) {
        return String.format("assessors/%s/files", assessor);
    }
}
