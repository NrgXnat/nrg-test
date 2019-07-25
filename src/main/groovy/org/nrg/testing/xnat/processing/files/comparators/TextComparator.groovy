package org.nrg.testing.xnat.processing.files.comparators

import org.nrg.testing.xnat.processing.exceptions.FileValidationException
import org.nrg.testing.xnat.processing.exceptions.ProcessingValidationException
import org.nrg.testing.xnat.processing.files.resources.ProcessingResourceFile

class TextComparator extends FileComparator {

    @Override
    void checkFileMatches(File secondaryFileDirectory, File file, ProcessingResourceFile processingResourceFile) throws ProcessingValidationException {
        final String actual = file.text.trim()
        if (processingResourceFile.expectedText != actual) {
            throw new FileValidationException("Expected text of ${processingResourceFile.expectedText} for file ${file.name} did not match actual text: ${actual}.")
        }
    }

    @Override
    boolean equals(Object o) {
        this == o || o instanceof TextComparator
    }

}
