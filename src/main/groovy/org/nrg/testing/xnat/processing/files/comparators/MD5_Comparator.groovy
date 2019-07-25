package org.nrg.testing.xnat.processing.files.comparators

import org.nrg.testing.FileIOUtils
import org.nrg.testing.xnat.processing.exceptions.FileValidationException
import org.nrg.testing.xnat.processing.exceptions.ProcessingValidationException
import org.nrg.testing.xnat.processing.files.resources.ProcessingResourceFile

class MD5_Comparator extends FileComparator {

    @Override
    void checkFileMatches(File secondaryFileDirectory, File file, ProcessingResourceFile processingResourceFile) throws ProcessingValidationException {
        try {
            final String calculatedMD5 = FileIOUtils.calculateMD5(file)
            if (calculatedMD5 != processingResourceFile.md5) {
                throw new FileValidationException("MD5 checksum did not match expected value for file: ${file.absolutePath}")
            }
        } catch (IOException ignored) {
            throw new FileValidationException("Could not process MD5 checksum for file: ${file.absolutePath}")
        }
    }

    @Override
    boolean equals(Object o) {
        this == o || o instanceof MD5_Comparator
    }

}
