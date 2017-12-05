package org.nrg.testing.xnat.processing.files.mutators;

import org.apache.commons.io.FileUtils;
import org.nrg.testing.xnat.conf.Settings;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ReplaceAllMutator extends FileMutator {

    private Map<String, String> replacements = new HashMap<>();

    public Map<String, String> getReplacements() {
        return replacements;
    }

    public void setReplacements(Map<String, String> replacements) {
        this.replacements = replacements;
    }

    @Override
    public File mutateFile(File file) {
        try {
            String fileContents = FileUtils.readFileToString(file, "UTF-8");
            for (Map.Entry<String, String> mapEntry : replacements.entrySet()) {
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
