package org.nrg.testing.xnat.processing.files.comparators.imaging;

import org.nrg.testing.xnat.processing.files.ImageProcessingException;

public class ImageDeviationComparator extends ImageComparator {

    private int maxGrayscaleDeviation;
    private int maxColorscaleDeviation;

    public ImageDeviationComparator(String deviations) {
        if (deviations == null) {
            maxGrayscaleDeviation = 0;
            maxColorscaleDeviation = 0;
        } else {
            String[] devArray = deviations.split(";");
            maxGrayscaleDeviation = Integer.parseInt(devArray[0]);
            maxColorscaleDeviation = Integer.parseInt(devArray[1]);
        }
    }

    @Override
    public String checkDiffedImage() throws ImageProcessingException {
        final int linearDeviation = diffedImage.getAbsoluteDeviation();
        if (diffedImage.isColor()) {
            if (linearDeviation > maxColorscaleDeviation) {
                return String.format("linear color deviation of %d exceeds maximum allowed of %d", linearDeviation, maxColorscaleDeviation);
            }
        } else {
            if (linearDeviation > maxGrayscaleDeviation) {
                return String.format("linear grayscale deviation of %d exceeds maximum allowed of %d", linearDeviation, maxGrayscaleDeviation);
            }
        }
        return null;
    }
}
