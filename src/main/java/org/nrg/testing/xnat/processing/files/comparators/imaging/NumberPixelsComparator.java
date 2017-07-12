package org.nrg.testing.xnat.processing.files.comparators.imaging;

import org.nrg.testing.xnat.processing.files.ImageProcessingException;

public class NumberPixelsComparator extends ImageComparator {

    private int maxDifferingPixels;

    public NumberPixelsComparator(String pixels) {
        maxDifferingPixels = (pixels == null) ? 0 : Integer.parseInt(pixels);
    }

    @Override
    public String checkDiffedImage() throws ImageProcessingException {
        final int differingPixels = diffedImage.getNumNonzeroPixels();
        if (differingPixels > maxDifferingPixels) {
            return String.format("%d pixels differed, more than the maximum allowed of %d", differingPixels, maxDifferingPixels);
        } else {
            return null;
        }
    }
}
