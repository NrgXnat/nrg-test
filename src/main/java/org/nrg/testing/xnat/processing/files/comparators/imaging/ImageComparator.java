/*
 * ImageComparator
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.testing.xnat.processing.files.comparators.imaging;

import org.nrg.testing.file.FileIO;
import org.nrg.testing.xnat.conf.Settings;
import org.nrg.testing.xnat.processing.files.ImageProcessingException;
import org.nrg.testing.xnat.processing.files.comparators.FileComparator;

import java.io.File;

public abstract class ImageComparator extends FileComparator {

    protected File originalImageFile;
    protected String fileNameRegex;
    protected String parentDir;
    protected DiffedImage diffedImage;

    public void setParentDir(String parentDir) {
        this.parentDir = parentDir;
    }

    public void setFileNameRegex(String fileNameRegex) {
        this.fileNameRegex = fileNameRegex;
    }

    private void findOriginalImage() throws ImageProcessingException {
        if (fileNameRegex == null) throw new RuntimeException("fileNameRegex must be set");
        originalImageFile = FileIO.recursiveFind(new File(Settings.TEMP_SUBDIR + File.separator + parentDir), fileNameRegex);
        if (originalImageFile == null) throw new ImageProcessingException("Could not find original file matching regex: " + fileNameRegex);
    }

    private void diffImages(File generatedFile) throws ImageProcessingException  {
        findOriginalImage();
        if (!generatedFile.exists()) throw new ImageProcessingException("Could not find generated image for: " + generatedFile.getName()); // both files are now guaranteed to exist if we make it here
        diffedImage = new DiffedImage(originalImageFile, generatedFile);
    }

    @Override
    public String checkFileMatches(File file) {
        try {
            diffImages(file);
            final String failureReason = checkDiffedImage();
            return (failureReason == null) ? null : String.format("Generated image file %s differs too much from expected file (%s).", file.getName(), failureReason);
        } catch (ImageProcessingException ipe) {
            return ipe.getMessage();
        }
    }

    /**
     * Check the image to make sure it's acceptable.
     * @return If unacceptable, return just the failure reason. If acceptable, return null
     * @throws ImageProcessingException If image is so unacceptable that it cannot be compared.
     */
    public abstract String checkDiffedImage() throws ImageProcessingException;

}
