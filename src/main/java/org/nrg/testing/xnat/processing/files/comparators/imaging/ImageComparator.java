package org.nrg.testing.xnat.processing.files.comparators.imaging;

import org.nrg.testing.xnat.processing.exceptions.FileValidationException;
import org.nrg.testing.xnat.processing.exceptions.ImageProcessingException;
import org.nrg.testing.xnat.processing.exceptions.ProcessingValidationException;
import org.nrg.testing.xnat.processing.files.comparators.FileComparator;
import org.nrg.testing.xnat.processing.files.resources.ProcessingResourceFile;

import java.io.File;
import java.nio.file.Paths;

public abstract class ImageComparator extends FileComparator {

    protected DiffedImage diffedImage;

    @Override
    public void checkFileMatches(File secondaryFileDirectory, File file, ProcessingResourceFile processingResourceFile) throws ProcessingValidationException {
        final String originalFileName = (processingResourceFile.getCompareTo() == null) ? file.getName() : processingResourceFile.getCompareTo();
        final File original = Paths.get(secondaryFileDirectory.getAbsolutePath(), originalFileName).toFile();
        if (!original.exists()) {
            throw new ImageProcessingException("Could not find original file: " + originalFileName);
        }
        diffedImage = new DiffedImage(original, file);
        try {
            checkDiffedImage();
            diffedImage = null; // dereference for garbage collection
        } catch (ImageProcessingException ipe) {
            diffedImage = null; // dereference for garbage collection
            throw new FileValidationException(String.format("Generated image file %s differs too much from expected file (%s): %s",  file, original, ipe.getMessage()));
        }
    }

    /**
     * Check the image to make sure it's acceptable.
     * @throws ImageProcessingException If image is unacceptable.
     */
    public abstract void checkDiffedImage() throws ImageProcessingException;

}
