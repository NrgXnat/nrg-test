package org.nrg.testing.xnat.processing.files.comparators.imaging

import com.fasterxml.jackson.annotation.JsonProperty
import groovy.transform.EqualsAndHashCode
import org.nrg.testing.xnat.processing.exceptions.ImageProcessingException

@EqualsAndHashCode
class ImageDeviationComparator extends ImageComparator {

    @JsonProperty('gray')  int maxGrayscaleDeviation = 0
    @JsonProperty('color') int maxColorscaleDeviation = 0

    @Override
    void checkDiffedImage(DiffedImage diffedImage) throws ImageProcessingException {
        final int oneNormDeviation = diffedImage.absoluteDeviation
        final int maxDeviation = (diffedImage.isColor) ? maxColorscaleDeviation : maxGrayscaleDeviation
        final String keyword = (diffedImage.isColor) ? 'color' : 'grayscale'
        if (oneNormDeviation > maxDeviation) {
            throw new ImageProcessingException("1-norm ${keyword} deviation of ${oneNormDeviation} exceeds maximum allowed of ${maxDeviation}")
        }
    }

}
