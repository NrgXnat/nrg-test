package org.nrg.testing.xnat.processing.files.comparators;

import org.apache.commons.io.FileUtils;
import org.nrg.testing.xnat.processing.exceptions.FileValidationException;
import org.nrg.testing.xnat.processing.exceptions.ProcessingValidationException;
import org.nrg.testing.xnat.processing.files.resources.ProcessingResourceFile;

import java.io.File;
import java.io.IOException;

public class TextComparator extends FileComparator {

    @Override
    public void checkFileMatches(File secondaryFileDirectory, File file, ProcessingResourceFile processingResourceFile) throws ProcessingValidationException {
        try {
            final String actual = FileUtils.readFileToString(file, "utf-8").trim();
            if (!processingResourceFile.getExpectedText().equals(actual)) {
                throw new FileValidationException(String.format("Expected text of %s for file %s did not match actual text: %s.", processingResourceFile.getExpectedText(), file.getName(), actual));
            }
        } catch (IOException ioe) {
            throw new FileValidationException("Failed to read file: " + file);
        }
    }

    @Override
    public boolean equals(Object o) {
        return (this == o || o instanceof TextComparator);
    }

}
