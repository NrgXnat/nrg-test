package org.nrg.testing.xnat.processing.files.comparators.imaging

import org.nrg.testing.xnat.processing.exceptions.FileValidationException
import org.nrg.testing.xnat.processing.exceptions.ImageProcessingException
import org.nrg.testing.xnat.processing.exceptions.ProcessingValidationException
import org.nrg.testing.xnat.processing.files.comparators.FileComparator
import org.nrg.testing.xnat.processing.files.resources.ProcessingResourceFile

import java.nio.file.Paths

abstract class ImageComparator extends FileComparator {

    @Override
    final void checkFileMatches(File secondaryFileDirectory, File file, ProcessingResourceFile processingResourceFile) throws ProcessingValidationException {
        final String originalFileName = processingResourceFile.compareTo ?: file.name
        final File original = Paths.get(secondaryFileDirectory.absolutePath, originalFileName).toFile()
        if (!original.exists()) {
            throw new ImageProcessingException("Could not find original file: ${originalFileName}")
        }
        try {
            checkDiffedImage(new DiffedImage(original, file))
        } catch (ImageProcessingException ipe) {
            throw new FileValidationException("Generated image file ${file} differs too much from expected file (${original}): ${ipe.message}")
        }
    }

    /**
     * Check the image to make sure it's acceptable.
     * @throws ImageProcessingException If image is unacceptable.
     */
    abstract void checkDiffedImage(DiffedImage diffedImage) throws ImageProcessingException

}
