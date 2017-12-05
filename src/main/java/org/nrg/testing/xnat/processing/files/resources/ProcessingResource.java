package org.nrg.testing.xnat.processing.files.resources;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.nrg.testing.xnat.rest.XnatRestDriver;
import org.nrg.xnat.pogo.experiments.ImagingSession;
import org.nrg.xnat.pogo.resources.Resource;

import java.util.List;

import static org.testng.AssertJUnit.fail;

public class ProcessingResource extends Resource {

    private String secondaryResources;
    private boolean isRegex = false;
    private String url;

    public String getSecondaryResources() {
        return secondaryResources;
    }

    public void setSecondaryResources(String secondaryResources) {
        this.secondaryResources = secondaryResources;
    }

    public boolean isRegex() {
        return isRegex;
    }

    public void setRegex(boolean regex) {
        isRegex = regex;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<ProcessingResourceFile> processingFiles() {
        return Lists.newArrayList(Iterables.filter(resourceFiles, ProcessingResourceFile.class));
    }

    public boolean requiresLocalCopy() {
        for (ProcessingResourceFile file : processingFiles()) {
            if (file.getComparator() != null) return true;
        }
        return false;
    }

    public Resource findActualResources(XnatRestDriver driver, ImagingSession session) {
        if (isRegex()) {
            final String actualLabel = driver.mainInterface().jsonQuery().get(driver.formatRestUrl("/experiments", session.getAccessionNumber(), "resources")).jsonPath().getString(String.format("ResultSet.Result.find { it.label.matches('%s') }.label", getFolder()));
            if (actualLabel == null) {
                fail("No resource folder matching given regex found");
            } else {
                setFolder(actualLabel);
            }
        }
        final Resource actualResource = new GenericResource(url).folder(getFolder());
        actualResource.setResourceFiles(driver.mainInterface().readResourceFiles(actualResource));
        return actualResource;
    }

    @Override
    public String resourceUrl() {
        return url;
    }

}
