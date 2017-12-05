package org.nrg.testing.xnat.processing.files.comparators.imaging;

import org.nrg.testing.xnat.processing.exceptions.ImageProcessingException;

public class PercentPixelsComparator extends ImageComparator {

    private double maxPercentError;
    private final static double IMPLICIT_TOLERANCE = 0.0000001;

    public double getMaxPercentError() {
        return maxPercentError;
    }

    public void setMaxPercentError(double maxPercentError) {
        this.maxPercentError = maxPercentError;
    }

    @Override
    public void checkDiffedImage() throws ImageProcessingException {
        final double differingPixels = diffedImage.getPercentNonzeroPixels();
        if (differingPixels > maxPercentError + IMPLICIT_TOLERANCE) {
            throw new ImageProcessingException(String.format("%f%% of pixels differed, more than the maximum allowed of %f%%", differingPixels, maxPercentError));
        }
    }

}
