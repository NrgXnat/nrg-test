/*
 * CSVGenerator
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.testing.xnat.processing;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import org.apache.commons.io.FilenameUtils;
import org.nrg.testing.file.FileIO;
import org.nrg.testing.util.RandomHelper;
import org.nrg.testing.xnat.conf.Settings;
import org.nrg.testing.xnat.processing.files.PipelineFile;
import org.nrg.testing.xnat.processing.files.ProcessingFileSet;
import org.nrg.testing.xnat.rest.XnatRestDriver;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class CSVGenerator {

    @SuppressWarnings("unused")
    public static void addMD5(String pathToCSV, String pathToFiles) {
        try {
            CSVReader csvReader = new CSVReader(new FileReader(pathToCSV), CSVReader.DEFAULT_SEPARATOR, CSVReader.DEFAULT_QUOTE_CHARACTER, '\0'); // turn off escaping to allow backslash);
            String outputCSV = String.format("%s%s%s_edited.csv", new File(pathToCSV).getParent(), File.separator, FilenameUtils.getBaseName(pathToCSV));
            CSVWriter writer = new CSVWriter(new FileWriter(outputCSV), ',', CSVWriter.NO_QUOTE_CHARACTER);
            List<String[]> outputLines = new ArrayList<>();
            for (String[] csvEntry : csvReader.readAll()) {
                String fileName = (csvEntry[0].contains("/")) ? csvEntry[0].substring(csvEntry[0].lastIndexOf("/") + 1) : csvEntry[0];
                System.out.println("Searching for file " + fileName);
                File file = FileIO.recursiveFind(new File(pathToFiles), fileName);
                String checksum = (file == null) ? "" : FileIO.calculateMD5(file);
                outputLines.add(new String[]{csvEntry[0], csvEntry[1], "MD5", checksum});
                System.out.println((checksum.equals("")) ? fileName + " not found" : "Added MD5 for " + fileName);
            }
            writer.writeAll(outputLines);
            writer.close();
            csvReader.close();
        } catch (IOException ioe) {
            System.out.println("Failed to form CSV " + ioe);
        }
    }

    @SuppressWarnings("unused")
    public static void extractCSV(XnatRestDriver xnatRestDriver, ProcessingFileSet pipelineFileSet, String session) {
        final String filesCall = xnatRestDriver.formatRestUrl("experiments", session, pipelineFileSet.restSubcall(xnatRestDriver));
        Set<PipelineFile> fileSet = new HashSet<>();

        List<Map<String, String>> fileMaps = Settings.mainCredentials()
                .get(filesCall + "?format=json").then().extract().path("ResultSet.Result");
        Map<String, String> pathCollectionMap = ProcessingFileSet.extractPathColMap(fileMaps);

        try {
            String csvName = RandomHelper.randomID(16);
            String outputCSV = String.format("%s%s%s.csv", Settings.TEMP_SUBDIR, File.separator, csvName);
            CSVWriter writer = new CSVWriter(new FileWriter(outputCSV), ',', CSVWriter.NO_QUOTE_CHARACTER);
            List<String[]> outputLines = new ArrayList<>();
            for (Map.Entry<String, String> pathCollectionEntry : pathCollectionMap.entrySet()) {
                outputLines.add(new String[]{pathCollectionEntry.getKey(), pathCollectionEntry.getValue()});
            }
            writer.writeAll(outputLines);
            writer.close();
            System.out.println("CSV output to: " + outputCSV);
        }
        catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
    }
}
