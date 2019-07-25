package org.nrg.testing.xnat.processing.files.comparators

import groovy.transform.EqualsAndHashCode
import org.nrg.testing.MathUtils
import org.nrg.testing.xnat.processing.exceptions.FileValidationException
import org.nrg.testing.xnat.processing.exceptions.ProcessingValidationException
import org.nrg.testing.xnat.processing.files.resources.ProcessingResourceFile

@EqualsAndHashCode
class FileSizeComparator extends FileComparator {

    double tolerance

    @Override
    void checkFileMatches(File secondaryFileDirectory, File file, ProcessingResourceFile processingResourceFile) throws ProcessingValidationException {
        final long actualSize = file.length()
        final long expectedSize = processingResourceFile.getExpectedSize()
        if (actualSize != expectedSize) {
            final double error = MathUtils.percentError(expectedSize, actualSize)
            if (error > tolerance) {
                throw new FileValidationException("The file size for the file ${file.name} had a value of ${actualSize}, which did not match the expected value of ${expectedSize} to within ${tolerance} percent error.")
            }
        }
    }

}
