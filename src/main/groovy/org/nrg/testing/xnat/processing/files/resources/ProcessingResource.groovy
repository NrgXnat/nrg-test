package org.nrg.testing.xnat.processing.files.resources

import org.nrg.testing.CollectionUtils
import org.nrg.testing.xnat.rest.XnatRestDriver
import org.nrg.xnat.pogo.experiments.ImagingSession
import org.nrg.xnat.pogo.resources.Resource

import static org.testng.AssertJUnit.fail

class ProcessingResource extends Resource {

    String secondaryResources
    boolean isRegex = false
    String url

    List<ProcessingResourceFile> processingFiles() {
        CollectionUtils.ofType(resourceFiles, ProcessingResourceFile)
    }

    boolean requiresLocalCopy() {
        processingFiles().any { processingFile ->
            processingFile.comparator != null
        }
    }

    Resource findActualResources(XnatRestDriver driver, ImagingSession session) {
        if (isRegex) {
            final String actualLabel = driver.mainInterface().jsonQuery().get(driver.formatRestUrl("/experiments/${session.accessionNumber}/resources")).jsonPath().getList("ResultSet.Result.label").find { label ->
                (label as String).matches(folder)
            }
            if (actualLabel == null) {
                fail('No resource folder matching given regex found')
            } else {
                setFolder(actualLabel)
            }
        }
        final Resource actualResource = new GenericResource(url).folder(folder)
        actualResource.resourceFiles(driver.mainInterface().readResourceFiles(actualResource))
    }

    @Override
    String resourceUrl() {
        url
    }

}
