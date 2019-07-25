package org.nrg.testing.xnat.processing.files.comparators.imaging

import groovy.transform.EqualsAndHashCode
import org.nrg.testing.xnat.processing.exceptions.ImageProcessingException

@EqualsAndHashCode
class NumberPixelsComparator extends ImageComparator {

    int maxDifferingPixels = 0

    @Override
    void checkDiffedImage(DiffedImage diffedImage) throws ImageProcessingException {
        final int differingPixels = diffedImage.numNonzeroPixels
        if (differingPixels > maxDifferingPixels) {
            throw new ImageProcessingException("${differingPixels} pixels differed, more than the maximum allowed of ${maxDifferingPixels}")
        }
    }

}
