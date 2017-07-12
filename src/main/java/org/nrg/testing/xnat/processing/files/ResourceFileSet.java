/*
 * org.nrg.selenium.pipeline.pipelineFileLocations.ResourceFileSet
 * XNAT http://www.xnat.org
 * Copyright (c) 2015, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified: 11/20/15 1:01 PM
 */

package org.nrg.testing.xnat.processing.files;

import org.nrg.testing.xnat.rest.XnatRestDriver;

import java.util.List;

import static org.testng.AssertJUnit.*;

public class ResourceFileSet extends ProcessingFileSet {

    private String resourceFolder;
    private boolean isRegex;

    public ResourceFileSet(String resourceFolder, boolean isRegex, String csvFileSetName, String session) {
        this.resourceFolder = resourceFolder;
        this.isRegex = isRegex;
        this.csvFileSetName = csvFileSetName;
        this.session = session;
    }

    @Override
    public String restSubcall(XnatRestDriver xnatRestDriver) {
        if (isRegex) {
            List<String> resources = xnatRestDriver.mainCredentials().get(xnatRestDriver.formatRestUrl("/experiments", session, "resources?format=json")).then().
                    extract().path("ResultSet.Result.label");
            for (String resource : resources) {
                if (resource.matches(resourceFolder)) {
                    return String.format("resources/%s/files", resource);
                }
            }
            fail("No resource folder matching given regex found");
            return null;
        } else {
            return String.format("resources/%s/files", resourceFolder);
        }
    }
}
