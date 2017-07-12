/*
 * org.nrg.selenium.pipeline.pipelineFileLocations.PipelineFileLocation
 * XNAT http://www.xnat.org
 * Copyright (c) 2015, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified: 10/30/15 5:41 PM
 */

package org.nrg.testing.xnat.processing.files;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.Logger;
import org.nrg.jira.testing_components.TestStatus;
import org.nrg.testing.CommonUtils;
import org.nrg.testing.file.FileIO;
import org.nrg.testing.util.IgnoreNullList;
import org.nrg.testing.util.RandomHelper;
import org.nrg.testing.xnat.conf.Settings;
import org.nrg.testing.xnat.processing.ProcessingCheckable;
import org.nrg.testing.xnat.processing.SessionRenewer;
import org.nrg.testing.xnat.processing.files.comparators.FileComparator;
import org.nrg.testing.xnat.processing.files.comparators.FileSizeComparator;
import org.nrg.testing.xnat.processing.files.comparators.MD5_Comparator;
import org.nrg.testing.xnat.processing.files.comparators.TextComparator;
import org.nrg.testing.xnat.processing.files.comparators.imaging.*;
import org.nrg.testing.xnat.processing.files.mutators.DecompressGzipMutator;
import org.nrg.testing.xnat.processing.files.mutators.ReplaceAllMutator;
import org.nrg.testing.xnat.processing.files.mutators.FileMutator;
import org.nrg.testing.xnat.rest.XnatRestDriver;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static org.testng.AssertJUnit.*;

public abstract class ProcessingFileSet implements ProcessingCheckable {

    protected String csvFileSetName;
    protected String session;

    public abstract String restSubcall(XnatRestDriver xnatRestDriver);

    public List<String> checkFilesMatch(String session, XnatRestDriver xnatRestDriver, SessionRenewer sessionRenewer) {
        IgnoreNullList<String> verificationErrors = new IgnoreNullList<>();
        final String filesCall = xnatRestDriver.formatRestUrl("experiments", session, restSubcall(xnatRestDriver));
        Set<PipelineFile> fileSet = new HashSet<>();

        List<Map<String, String>> fileMaps = xnatRestDriver.mainCredentials().get(filesCall + "?format=json").then().extract().path("ResultSet.Result");
        Map<String, String> pathCollectionMap = extractPathColMap(fileMaps);

        if (!fileMaps.isEmpty()) xnatRestDriver.passStep();
        else fail("Did not find required files from REST call: " + filesCall);

        String zipFileName = RandomHelper.randomID(20);
        grabCSV(xnatRestDriver);
        xnatRestDriver.passStep();

        boolean primaryDownloadRequired = false;
        boolean secondaryDownloadRequired = false;
        String secondaryZip = RandomHelper.randomID(20);

        try {
            CSVReader csvReader = new CSVReader(new FileReader(Settings.TEMP_SUBDIR + File.separator + csvFileSetName), CSVReader.DEFAULT_SEPARATOR, CSVReader.DEFAULT_QUOTE_CHARACTER, '\0'); // turn off escaping to allow backslash
            for (String[] csvEntry : csvReader.readAll()) { // csvEntry[0] = pathRegex, csvEntry[1] = collection, csvEntry[2] = FileComparator, csvEntry[3] = FileComparator data
                PipelineFile file = null;
                for (Map.Entry<String, String> pathCollectionEntry : pathCollectionMap.entrySet()) {
                    if (pathCollectionEntry.getKey().matches(csvEntry[0]) && pathCollectionEntry.getValue().matches(csvEntry[1])) { // path and collection are both regex
                        FileComparator fileComparator = null;
                        if (csvEntry.length > 2) {
                            primaryDownloadRequired = true;
                            FileMutator fileMutator = null;
                            if (csvEntry.length > 4) { // if there exists a mutator in addition to a comparator
                                switch (csvEntry[4]) {
                                    case "replaceAll":
                                        fileMutator = new ReplaceAllMutator(csvEntry[5]);
                                        break;
                                    case "ungzip":
                                        fileMutator = new DecompressGzipMutator();
                                        break;
                                    default:
                                        fail("Required FileMutator not supported. Unknown value " + csvEntry[4]);
                                }
                            }
                            String deviation;
                            switch (csvEntry[2]) {
                                case "MD5":
                                    fileComparator = new MD5_Comparator(csvEntry[3]);
                                    break;
                                case "ImageDeviation":
                                    deviation = (csvEntry.length > 3) ? csvEntry[3] : "0;0";
                                    fileComparator = new ImageDeviationComparator(deviation); // default to pixel-wise equality
                                    break;
                                case "NumPixels":
                                    deviation = (csvEntry.length > 3) ? csvEntry[3] : "0";
                                    fileComparator = new NumberPixelsComparator(deviation);
                                    break;
                                case "PercentPixels":
                                    deviation = (csvEntry.length > 3) ? csvEntry[3] : "0";
                                    fileComparator = new PercentPixelsComparator(deviation);
                                    break;
                                case "Cluster":
                                    fileComparator = new PixelClusterComparator(csvEntry[3]);
                                    break;
                                case "FileSize":
                                    fileComparator = new FileSizeComparator(csvEntry[3]);
                                    break;
                                case "TextEquals":
                                    fileComparator = new TextComparator(csvEntry[3]);
                                    break;
                                default:
                                    fail("Required FileComparator not supported. Unknown value " + csvEntry[2]);
                                    break;
                            }
                            if (fileComparator instanceof ImageComparator) {
                                secondaryDownloadRequired = true;
                                ((ImageComparator) fileComparator).setParentDir(secondaryZip);
                                ((ImageComparator) fileComparator).setFileNameRegex(CommonUtils.last(csvEntry[0].split("/")));
                            }
                            fileComparator.addMutator(fileMutator);
                        }
                        file = new PipelineFile(pathCollectionEntry.getKey(), csvEntry[1], fileComparator); // path from XNAT instead of csvEntry[0] to replace regex
                        break;
                    }
                }
                if (file == null) verificationErrors.add(String.format("File (%s, %s) not found in list pulled from XNAT.", csvEntry[0], csvEntry[1]));
                else fileSet.add(file);
            }
            if (primaryDownloadRequired) {
                FileIO.grabFromXNAT(xnatRestDriver.mainCredentials(), filesCall + "?format=zip", Settings.TEMP_SUBDIR + File.separator + zipFileName + ".zip");
                FileIO.unzip(Settings.TEMP_SUBDIR, zipFileName + ".zip");
                xnatRestDriver.passStep();
            }
            if (secondaryDownloadRequired) {
                downloadSecondaryFiles(secondaryZip, xnatRestDriver);
            }
            csvReader.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read in list of files to assert.");
        }

        StopWatch sessionTimeoutChecker = CommonUtils.launchStopWatch();
        for (Iterator<PipelineFile> iterator = fileSet.iterator(); iterator.hasNext();) {
            final PipelineFile pipelineFile = iterator.next();
            iterator.remove(); // remove PipelineFile to free up memory
            verificationErrors.add(pipelineFile.checkComparatorSatisfied(Settings.TEMP_SUBDIR + File.separator + zipFileName));
            if (CommonUtils.maxTimeReached(sessionTimeoutChecker, 10*60)) {
                if (sessionRenewer != null) sessionRenewer.renewSession();
                sessionTimeoutChecker = CommonUtils.launchStopWatch();
            }
        }

        if (verificationErrors.isEmpty()) {
            Logger.getLogger(ProcessingFileSet.class).info("All files present and valid for " + csvFileSetName);
            xnatRestDriver.passStep();
        } else {
            xnatRestDriver.captureStep(TestStatus.FAIL, verificationErrors.join());
        }
        return verificationErrors;
    }

    protected void grabCSV(XnatRestDriver xnatDriver) {
        String csvPath = Settings.TEMP_SUBDIR + File.separator + csvFileSetName; // ensure we download a new copy for each run by sticking it in temp
        FileIO.grabFromXNAT(xnatDriver.mainCredentials(), xnatDriver.formatRestUrl("experiments", session, "/resources/validation/files", csvFileSetName), csvPath);
    }

    private void downloadSecondaryFiles(String zipFileName, XnatRestDriver xnatRestDriver) {
        final String csvName = csvFileSetName.split("\\.")[0]; // trim file extension
        FileIO.grabFromXNAT(xnatRestDriver.mainCredentials(), xnatRestDriver.formatRestUrl("experiments", session, "resources", csvName, "files?format=zip"), Settings.TEMP_SUBDIR + File.separator + zipFileName + ".zip");
        FileIO.unzip(Settings.TEMP_SUBDIR, zipFileName + ".zip");
        xnatRestDriver.passStep();
    }

    public static Map<String, String> extractPathColMap(List<Map<String, String>> fileMaps) {
        Map<String, String> pathCollectionMap = new HashMap<>();

        for (Map<String, String> fileMap : fileMaps) {
            String uri = fileMap.get("URI");
            String path = uri.substring(uri.indexOf("files/") + 6);
            String collection = fileMap.get("collection");
            pathCollectionMap.put(path, collection);
        }

        return pathCollectionMap;
    }

}
