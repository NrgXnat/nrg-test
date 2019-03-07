package org.nrg.testing.xnat.processing.files;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.Logger;
import org.nrg.jira.testing_components.TestStatus;
import org.nrg.testing.CommonUtils;
import org.nrg.testing.file.FileIO;
import org.nrg.testing.util.IgnoreNullList;
import org.nrg.testing.util.RandomHelper;
import org.nrg.testing.xnat.conf.Settings;
import org.nrg.testing.xnat.processing.SessionRenewer;
import org.nrg.testing.xnat.processing.exceptions.FileValidationException;
import org.nrg.testing.xnat.processing.exceptions.ProcessingValidationException;
import org.nrg.testing.xnat.processing.files.comparators.FileComparator;
import org.nrg.testing.xnat.processing.files.mutators.FileMutator;
import org.nrg.testing.xnat.processing.files.resources.GenericResource;
import org.nrg.testing.xnat.processing.files.resources.ProcessingResource;
import org.nrg.testing.xnat.processing.files.resources.ProcessingResourceFile;
import org.nrg.testing.xnat.rest.XnatRestDriver;
import org.nrg.xnat.pogo.experiments.ImagingSession;
import org.nrg.xnat.pogo.resources.Resource;
import org.nrg.xnat.pogo.resources.ResourceFile;
import org.nrg.xnat.pogo.resources.SubjectAssessorResource;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AssessorFileSet.class, name = "assessor_xsi"),
        @JsonSubTypes.Type(value = SessionFileSet.class, name = "session"),
        @JsonSubTypes.Type(value = ScanFileSet.class, name = "scan")
})
public abstract class ProcessingFileSets {

    private List<ProcessingResource> resources = new ArrayList<>();
    private Map<String, FileMutator> mutators = new HashMap<>();
    private Map<String, FileComparator> comparators = new HashMap<>();

    public List<ProcessingResource> getResources() {
        return resources;
    }

    public void setResources(List<ProcessingResource> resources) {
        this.resources = resources;
    }

    public Map<String, FileMutator> getMutators() {
        return mutators;
    }

    public void setMutators(Map<String, FileMutator> mutators) {
        this.mutators = mutators;
    }

    public Map<String, FileComparator> getComparators() {
        return comparators;
    }

    public void setComparators(Map<String, FileComparator> comparators) {
        this.comparators = comparators;
    }

    public List<String> validate(XnatRestDriver xnatRestDriver, ImagingSession session, SessionRenewer sessionRenewer) {
        final IgnoreNullList<String> verificationErrors = new IgnoreNullList<>();
        final String resourceUrlBase = findBaseUrlForResources(xnatRestDriver, session);
        sessionRenewer.startTimer();
        for (ProcessingResource processingResource : resources) {
            processingResource.setUrl(resourceUrlBase);
            final Resource actualResource = processingResource.findActualResources(xnatRestDriver, session);
            final File primaryResources = processingResource.requiresLocalCopy() ? downloadActualResources(xnatRestDriver, actualResource) : null;
            final File secondaryResources = downloadSecondaryResources(xnatRestDriver, session, processingResource);
            for (Iterator<ProcessingResourceFile> fileIterator = processingResource.processingFiles().iterator(); fileIterator.hasNext();) {
                final ProcessingResourceFile file = fileIterator.next();
                final ResourceFile actualFileObject = file.isRegex() ? actualResource.findFileMatch(file.fullPath()) : actualResource.findFile(file.fullPath());
                if (actualFileObject == null) {
                    verificationErrors.add(String.format("File %s not found in %s resource pulled from XNAT.", file.fullPath(), processingResource.getFolder()));
                } else {
                    final String comparatorKey = file.getComparator();
                    if (comparatorKey != null) {
                        final FileComparator comparator = comparators.get(comparatorKey);
                        final String mutatorKey = file.getMutator();
                        final File actualFile = primaryResources.toPath().resolve(actualFileObject.fullPath()).toFile();
                        try {
                            if (!actualFile.exists()) throw new FileValidationException("Could not find file downloaded for validation: " + actualFileObject.fullPath());
                            comparator.checkFileMatches(secondaryResources, mutatorKey == null ? actualFile : mutators.get(mutatorKey).mutateFile(actualFile), file);
                        } catch (ProcessingValidationException pve) {
                            verificationErrors.add(pve.getMessage());
                        }
                    }
                }

                sessionRenewer.checkAndRenewTimer();
                fileIterator.remove(); // dereference ProcessingResourceFile to allow garbage collection
            }
        }
        if (verificationErrors.isEmpty()) {
            Logger.getLogger(ProcessingFileSets.class).info("All files present and valid for files defined in resources: " + resources);
            xnatRestDriver.passStep();
        } else {
            xnatRestDriver.captureStep(TestStatus.FAIL, verificationErrors.join());
        }
        return verificationErrors;
    }

    public abstract String findBaseUrlForResources(XnatRestDriver driver, ImagingSession session);

    public abstract int numIntermediateLocalResourceFolders();

    private File downloadActualResources(XnatRestDriver restDriver, Resource actualResource) {
        return handleDownload(restDriver, actualResource, numIntermediateLocalResourceFolders());
    }

    private File downloadSecondaryResources(XnatRestDriver restDriver, ImagingSession session, ProcessingResource processingResource) {
        final String secondaryResources = processingResource.getSecondaryResources();
        if (secondaryResources == null) {
            return null;
        } else {
            final ProcessingFileSets secondaryFileSets = new SessionFileSet();
            final Resource resource = new GenericResource(secondaryFileSets.findBaseUrlForResources(restDriver, session)).folder(secondaryResources);
            return handleDownload(restDriver, resource, secondaryFileSets.numIntermediateLocalResourceFolders());
        }
    }

    private File handleDownload(XnatRestDriver restDriver, Resource resource, int numSubdirs) {
        final String folderName = RandomHelper.randomID(20);
        final File zip = Paths.get(Settings.TEMP_SUBDIR, folderName + ".zip").toFile();
        restDriver.saveBinaryResponseToFile(restDriver.mainInterface().queryBase().queryParam("format", "zip").queryParam("structure", "simplified").get(restDriver.resourceFilesUrl(resource)), zip);
        final Path baseLocalPath = Paths.get(Settings.TEMP_SUBDIR, folderName);
        FileIO.unzip(baseLocalPath, zip);
        return iterateSubdirs(baseLocalPath.toFile(), numSubdirs).toPath().resolve(resource.getFolder()).toFile();
    }

    private File iterateSubdirs(File baseDir, int numIterations) {
        File currentDir = baseDir;
        for (int i = 0; i < numIterations; i++) {
            currentDir = currentDir.listFiles()[0];
        }
        return currentDir;
    }

}
