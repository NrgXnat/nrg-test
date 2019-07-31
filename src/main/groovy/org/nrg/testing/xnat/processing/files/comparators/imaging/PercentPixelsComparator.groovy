package org.nrg.testing.xnat.processing.files.comparators.imaging

import groovy.transform.EqualsAndHashCode
import org.nrg.testing.xnat.processing.exceptions.ImageProcessingException

@EqualsAndHashCode
class PercentPixelsComparator extends ImageComparator {

    double maxPercentError

    @Override
    void checkDiffedImage(DiffedImage diffedImage) throws ImageProcessingException {
        final double differingPixels = diffedImage.percentNonzeroPixels
        if (differingPixels > maxPercentError) {
            throw new ImageProcessingException("${differingPixels.round(4)}% of pixels differed, more than the maximum allowed of ${maxPercentError}%")
        }
    }

}
