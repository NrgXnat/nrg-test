package org.nrg.testing.xnat.processing.files.comparators.imaging

class PixelFactory {

    public static final ComparisonPixel ZERO_GRAY_PIXEL = new ZeroDiffPixel(false)
    public static final ComparisonPixel ZERO_COLOR_PIXEL = new ZeroDiffPixel(true)

    static ComparisonPixel getPixel(int sourceGray, int generatedGray, boolean storeFullInformation) {
        if (storeFullInformation) {
            (sourceGray == generatedGray) ? new ZeroComparisonPixel(sourceGray) : new ComparisonPixel(sourceGray, generatedGray)
        } else {
            (sourceGray == generatedGray) ? ZERO_GRAY_PIXEL : new DiffedPixel(sourceGray, generatedGray)
        }
    }

    static ComparisonPixel getPixel(int sourceGray, int generatedGray) {
        getPixel(sourceGray, generatedGray, false)
    }

    static ComparisonPixel getPixel(int sourceRed, int sourceGreen, int sourceBlue, int generatedRed, int generatedGreen, int generatedBlue, boolean storeFullInformation) {
        if (storeFullInformation) {
            if (sourceRed == generatedRed && sourceGreen == generatedGreen && sourceBlue == generatedBlue) {
                new ZeroComparisonPixel(sourceRed, sourceGreen, sourceBlue)
            } else {
                new ComparisonPixel(sourceRed, sourceGreen, sourceBlue, generatedRed, generatedGreen, generatedBlue)
            }
        } else {
            if (sourceRed == generatedRed && sourceGreen == generatedGreen && sourceBlue == generatedBlue) {
                ZERO_COLOR_PIXEL
            } else {
                new ComparisonPixel(sourceRed, sourceGreen, sourceBlue, generatedRed, generatedGreen, generatedBlue)
            }
        }
    }

    static ComparisonPixel getPixel(int sourceRed, int sourceGreen, int sourceBlue, int generatedRed, int generatedGreen, int generatedBlue) {
        getPixel(sourceRed, sourceGreen, sourceBlue, generatedRed, generatedGreen, generatedBlue, false)
    }

}
