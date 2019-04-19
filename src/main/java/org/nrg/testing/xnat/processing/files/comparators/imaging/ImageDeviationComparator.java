package org.nrg.testing.xnat.processing.files.comparators.imaging;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.nrg.testing.xnat.processing.exceptions.ImageProcessingException;

public class ImageDeviationComparator extends ImageComparator {

    @JsonProperty("gray")  private int maxGrayscaleDeviation = 0;
    @JsonProperty("color") private int maxColorscaleDeviation = 0;

    public int getMaxGrayscaleDeviation() {
        return maxGrayscaleDeviation;
    }

    public void setMaxGrayscaleDeviation(int maxGrayscaleDeviation) {
        this.maxGrayscaleDeviation = maxGrayscaleDeviation;
    }

    public int getMaxColorscaleDeviation() {
        return maxColorscaleDeviation;
    }

    public void setMaxColorscaleDeviation(int maxColorscaleDeviation) {
        this.maxColorscaleDeviation = maxColorscaleDeviation;
    }

    @Override
    public void checkDiffedImage() throws ImageProcessingException {
        final int oneNormDeviation = diffedImage.getAbsoluteDeviation();
        if (diffedImage.isColor() && oneNormDeviation > maxColorscaleDeviation) {
            throw new ImageProcessingException(String.format("1-norm color deviation of %d exceeds maximum allowed of %d", oneNormDeviation, maxColorscaleDeviation));
        } else if (!diffedImage.isColor() && oneNormDeviation > maxGrayscaleDeviation) {
            throw new ImageProcessingException(String.format("1-norm grayscale deviation of %d exceeds maximum allowed of %d", oneNormDeviation, maxGrayscaleDeviation));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImageDeviationComparator)) return false;

        ImageDeviationComparator that = (ImageDeviationComparator) o;

        return maxGrayscaleDeviation == that.maxGrayscaleDeviation && maxColorscaleDeviation == that.maxColorscaleDeviation;
    }

    @Override
    public int hashCode() {
        int result = maxGrayscaleDeviation;
        result = 31 * result + maxColorscaleDeviation;
        return result;
    }

}
