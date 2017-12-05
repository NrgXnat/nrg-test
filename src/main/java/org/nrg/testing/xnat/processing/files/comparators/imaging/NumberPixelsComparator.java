package org.nrg.testing.xnat.processing.files.comparators.imaging;

import org.nrg.testing.xnat.processing.exceptions.ImageProcessingException;

public class NumberPixelsComparator extends ImageComparator {

    private int maxDifferingPixels = 0;

    public int getMaxDifferingPixels() {
        return maxDifferingPixels;
    }

    public void setMaxDifferingPixels(int maxDifferingPixels) {
        this.maxDifferingPixels = maxDifferingPixels;
    }

    @Override
    public void checkDiffedImage() throws ImageProcessingException {
        final int differingPixels = diffedImage.getNumNonzeroPixels();
        if (differingPixels > maxDifferingPixels) {
            throw new ImageProcessingException(String.format("%d pixels differed, more than the maximum allowed of %d", differingPixels, maxDifferingPixels));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NumberPixelsComparator)) return false;

        NumberPixelsComparator that = (NumberPixelsComparator) o;

        return maxDifferingPixels == that.maxDifferingPixels;
    }

    @Override
    public int hashCode() {
        return maxDifferingPixels;
    }

}
