package org.nrg.testing.xnat.processing.files.comparators;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class TextComparator extends FileComparator {
    private final String expectedText;

    public TextComparator(String expectedText) {
        this.expectedText = expectedText;
    }

    @Override
    public String checkFileMatches(File file) {
        try {
            final String actual = FileUtils.readFileToString(file, "utf-8").trim();
            return (expectedText.equals(actual)) ? null : String.format("Expected text of %s for file %s did not match actual text: %s.", expectedText, file.getName(), actual);
        } catch (IOException ioe) {
            return "IOException when checking file: " + file.getName();
        }
    }
}
