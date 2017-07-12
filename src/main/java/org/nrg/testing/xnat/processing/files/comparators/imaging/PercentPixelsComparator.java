package org.nrg.testing.xnat.processing.files.comparators.imaging;

import org.nrg.testing.xnat.processing.files.ImageProcessingException;

public class PercentPixelsComparator extends ImageComparator {

    private double maxPixelPercentError;
    private final static double IMPLICIT_TOLERANCE = 0.0000001;

    public PercentPixelsComparator(String pixels) {
        maxPixelPercentError = (pixels == null) ? 0 : Double.parseDouble(pixels);
    }

    @Override
    public String checkDiffedImage() throws ImageProcessingException {
        final double differingPixels = diffedImage.getPercentNonzeroPixels();
        if (differingPixels > maxPixelPercentError + IMPLICIT_TOLERANCE) {
            return String.format("%f%% of pixels differed, more than the maximum allowed of %f%%", differingPixels, maxPixelPercentError);
        } else {
            return null;
        }
    }
}
