package org.nrg.testing.xnat.processing.files

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.apache.log4j.Logger
import org.nrg.jira.components.zephyr.TestStatus
import org.nrg.testing.HttpUtils
import org.nrg.testing.file.FileIO
import org.nrg.testing.util.IgnoreNullList
import org.nrg.testing.util.RandomHelper
import org.nrg.testing.xnat.conf.Settings
import org.nrg.testing.xnat.processing.SessionRenewer
import org.nrg.testing.xnat.processing.exceptions.FileValidationException
import org.nrg.testing.xnat.processing.exceptions.ProcessingValidationException
import org.nrg.testing.xnat.processing.files.comparators.FileComparator
import org.nrg.testing.xnat.processing.files.mutators.FileMutator
import org.nrg.testing.xnat.processing.files.resources.GenericResource
import org.nrg.testing.xnat.processing.files.resources.ProcessingResource
import org.nrg.testing.xnat.processing.files.resources.ProcessingResourceFile
import org.nrg.testing.xnat.rest.XnatRestDriver
import org.nrg.xnat.pogo.experiments.ImagingSession
import org.nrg.xnat.pogo.resources.Resource
import org.nrg.xnat.pogo.resources.ResourceFile

import java.nio.file.Path
import java.nio.file.Paths

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = 'type'
)
@JsonSubTypes([
        @JsonSubTypes.Type(value = AssessorFileSet, name = 'assessor_xsi'),
        @JsonSubTypes.Type(value = SessionFileSet, name = 'session'),
        @JsonSubTypes.Type(value = ScanFileSet, name = 'scan')
])
abstract class ProcessingFileSets {

    private static final Logger LOGGER = Logger.getLogger(ProcessingFileSets)
    List<ProcessingResource> resources = []
    Map<String, FileMutator> mutators = [:]
    Map<String, FileComparator> comparators = [:]

    List<String> validate(XnatRestDriver xnatRestDriver, ImagingSession session, SessionRenewer sessionRenewer) {
        final IgnoreNullList<String> verificationErrors = new IgnoreNullList<>()
        final String resourceUrlBase = findBaseUrlForResources(xnatRestDriver, session)
        sessionRenewer.startTimer()
        resources.each { processingResource ->
            processingResource.setUrl(resourceUrlBase)
            final Resource actualResource = processingResource.findActualResources(xnatRestDriver, session)
            final File primaryResources = processingResource.requiresLocalCopy() ? downloadActualResources(xnatRestDriver, actualResource) : null
            final File secondaryResources = downloadSecondaryResources(xnatRestDriver, session, processingResource)
            for (Iterator<ProcessingResourceFile> fileIterator = processingResource.processingFiles().iterator(); fileIterator.hasNext();) { // use an iterator to delete items from list ASAP to save memory
                final ProcessingResourceFile processingResourceFile = fileIterator.next()
                final ResourceFile actualFileObject = processingResourceFile.isRegex() ? actualResource.findFileMatch(processingResourceFile.fullPath()) : actualResource.findFile(processingResourceFile.fullPath())
                if (actualFileObject == null) {
                    verificationErrors << "File ${processingResourceFile.fullPath()} not found in ${processingResource.folder} resource pulled from XNAT.".toString()
                } else {
                    final String comparatorKey = processingResourceFile.comparator
                    if (comparatorKey != null) {
                        final FileComparator comparator = comparators.get(comparatorKey)
                        final String mutatorKey = processingResourceFile.mutator
                        final File actualFile = primaryResources.toPath().resolve(actualFileObject.fullPath()).toFile()
                        try {
                            if (!actualFile.exists()) {
                                throw new FileValidationException("Could not find file downloaded for validation: ${actualFileObject.fullPath()}")
                            }
                            comparator.checkFileMatches(secondaryResources, mutatorKey == null ? actualFile : mutators[mutatorKey].mutateFile(actualFile), processingResourceFile)
                        } catch (ProcessingValidationException pve) {
                            verificationErrors << pve.message
                        }
                    }
                }

                sessionRenewer.checkAndRenewTimer()
                fileIterator.remove() // dereference ProcessingResourceFile to allow garbage collection
            }
        }
        if (verificationErrors.isEmpty()) {
            LOGGER.info("All files present and valid for files defined in resources: ${resources}")
            xnatRestDriver.passStep()
        } else {
            xnatRestDriver.captureStep(TestStatus.FAIL, verificationErrors.join())
        }
        verificationErrors
    }

    abstract String findBaseUrlForResources(XnatRestDriver driver, ImagingSession session)

    abstract int numIntermediateLocalResourceFolders()

    private File downloadActualResources(XnatRestDriver restDriver, Resource actualResource) {
        handleDownload(restDriver, actualResource, numIntermediateLocalResourceFolders())
    }

    private File downloadSecondaryResources(XnatRestDriver restDriver, ImagingSession session, ProcessingResource processingResource) {
        final String secondaryResources = processingResource.secondaryResources
        if (secondaryResources == null) {
            null
        } else {
            final ProcessingFileSets secondaryFileSets = new SessionFileSet()
            final Resource resource = new GenericResource(secondaryFileSets.findBaseUrlForResources(restDriver, session)).folder(secondaryResources)
            handleDownload(restDriver, resource, secondaryFileSets.numIntermediateLocalResourceFolders())
        }
    }

    private File handleDownload(XnatRestDriver restDriver, Resource resource, int numSubdirs) {
        final String folderName = RandomHelper.randomID(20)
        final File zip = Paths.get(Settings.TEMP_SUBDIR, "${folderName}.zip").toFile()
        HttpUtils.saveBinaryResponseToFile(restDriver.mainInterface().queryBase().queryParam('format', 'zip').queryParam('structure', 'simplified').get(restDriver.resourceFilesUrl(resource)), zip)
        final Path baseLocalPath = Paths.get(Settings.TEMP_SUBDIR, folderName)
        FileIO.unzip(baseLocalPath, zip)
        iterateSubdirs(baseLocalPath.toFile(), numSubdirs).toPath().resolve(resource.folder).toFile()
    }

    private File iterateSubdirs(File baseDir, int numIterations) {
        File currentDir = baseDir
        numIterations.times {
            currentDir = currentDir.listFiles()[0]
        }
        currentDir
    }

}
