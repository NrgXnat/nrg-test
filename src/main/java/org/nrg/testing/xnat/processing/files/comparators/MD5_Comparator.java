package org.nrg.testing.xnat.processing.files.comparators;

import org.nrg.testing.file.FileIO;
import org.nrg.testing.xnat.processing.exceptions.FileValidationException;
import org.nrg.testing.xnat.processing.exceptions.ProcessingValidationException;
import org.nrg.testing.xnat.processing.files.resources.ProcessingResourceFile;

import java.io.File;
import java.io.IOException;

public class MD5_Comparator extends FileComparator {

    @Override
    public void checkFileMatches(File secondaryFileDirectory, File file, ProcessingResourceFile processingResourceFile) throws ProcessingValidationException {
        try {
            final String calculatedMD5 = FileIO.calculateMD5(file);
            if (!calculatedMD5.equals(processingResourceFile.getMd5())) throw new FileValidationException("MD5 checksum did not match expected value for file: " + file.getAbsolutePath());
        } catch (IOException ioe) {
            throw new FileValidationException("Could not process MD5 checksum for file: " + file.getAbsolutePath());
        }
    }

    @Override
    public boolean equals(Object o) {
        return (this == o || o instanceof MD5_Comparator);
    }

}
