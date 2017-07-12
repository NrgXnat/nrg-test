/*
 * ReplaceAllMutator
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.testing.xnat.processing.files.mutators;

import org.apache.commons.io.FileUtils;
import org.nrg.testing.xnat.conf.Settings;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ReplaceAllMutator extends FileMutator {

    private Map<String, String> matchRegexMap = new HashMap<>();

    public ReplaceAllMutator(String inputString) {
        String[] splitString = inputString.split(",");
        if (splitString.length % 2 == 1) throw new RuntimeException("ReplaceAllMutator cannot create a map from an odd number of Strings");
        if (splitString.length == 0) throw new RuntimeException("Nothing to split because there's no commas.");
        for (int i = 0; i < splitString.length; i += 2) {
            matchRegexMap.put(splitString[i], splitString[i + 1]);
        }
    }

    @Override
    public File mutateFile(File file) {
        try {
            String fileContents = FileUtils.readFileToString(file, "UTF-8");
            for (Map.Entry<String, String> mapEntry : matchRegexMap.entrySet()) {
                fileContents = fileContents.replaceAll(mapEntry.getKey(), mapEntry.getValue());
            }
            fileContents = fileContents.replaceAll(Settings.EMAIL, "EMAIL");
            FileUtils.writeStringToFile(file, fileContents, "UTF-8");
            return file;
        } catch (IOException ioe) {
            throw new RuntimeException("Could not replace text in file " + file, ioe);
        }
    }
}
