package org.nrg.testing.xnat.processing.files.comparators.imaging;


public class PixelFactory {

    public static final ComparisonPixel ZERO_GRAY_PIXEL = new ZeroDiffPixel(false);
    public static final ComparisonPixel ZERO_COLOR_PIXEL = new ZeroDiffPixel(true);

    public static ComparisonPixel getPixel(int sourceGray, int generatedGray, boolean storeFullInformation) {
        if (storeFullInformation) {
            return (sourceGray == generatedGray) ? new ZeroComparisonPixel(sourceGray) : new ComparisonPixel(sourceGray, generatedGray);
        } else {
            return (sourceGray == generatedGray) ? ZERO_GRAY_PIXEL : new DiffedPixel(sourceGray, generatedGray);
        }
    }

    public static ComparisonPixel getPixel(int sourceGray, int generatedGray) {
        return getPixel(sourceGray, generatedGray, false);
    }

    public static ComparisonPixel getPixel(int sourceRed, int sourceGreen, int sourceBlue, int generatedRed, int generatedGreen, int generatedBlue, boolean storeFullInformation) {
        if (storeFullInformation) {
            if (sourceRed == generatedRed && sourceGreen == generatedGreen && sourceBlue == generatedBlue) {
                return new ZeroComparisonPixel(sourceRed, sourceGreen, sourceBlue);
            } else {
                return new ComparisonPixel(sourceRed, sourceGreen, sourceBlue, generatedRed, generatedGreen, generatedBlue);
            }
        } else {
            if (sourceRed == generatedRed && sourceGreen == generatedGreen && sourceBlue == generatedBlue) {
                return ZERO_COLOR_PIXEL;
            } else {
                return new ComparisonPixel(sourceRed, sourceGreen, sourceBlue, generatedRed, generatedGreen, generatedBlue);
            }
        }
    }

    public static ComparisonPixel getPixel(int sourceRed, int sourceGreen, int sourceBlue, int generatedRed, int generatedGreen, int generatedBlue) {
        return getPixel(sourceRed, sourceGreen, sourceBlue, generatedRed, generatedGreen, generatedBlue, false);
    }
}
