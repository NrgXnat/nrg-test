package org.nrg.testing.xnat.processing.files;

import org.nrg.testing.xnat.rest.XnatRestDriver;
import org.nrg.xnat.pogo.experiments.ImagingSession;

public class AssessorFileSet extends ProcessingFileSets {

    private String xsiType;

    public String getXsiType() {
        return xsiType;
    }

    public void setXsiType(String xsiType) {
        this.xsiType = xsiType;
    }

    @Override
    public String findBaseUrlForResources(XnatRestDriver driver, ImagingSession session) {
        return String.format("/data/experiments/%s/assessors/%s", session.getAccessionNumber(), xsiType);
    }

}
