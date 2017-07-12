package org.nrg.testing.xnat.processing.files.comparators.imaging;

public class DiffedPixel extends ComparisonPixel {

    protected int redDiff, greenDiff, blueDiff, grayDiff;

    public DiffedPixel(int sourceRed, int sourceGreen, int sourceBlue, int generatedRed, int generatedGreen, int generatedBlue) {
        redDiff = sourceRed - generatedRed;
        greenDiff = sourceGreen - generatedGreen;
        blueDiff = sourceBlue - generatedBlue;
        isColor = true;
    }

    public DiffedPixel(int sourceGray, int generatedGray) {
        grayDiff = sourceGray - generatedGray;
        isColor = false;
    }

    protected DiffedPixel() {}

    @Override
    public int getGrayDiff() {
        return grayDiff;
    }

    @Override
    public int getRedDiff() {
        return redDiff;
    }

    @Override
    public int getGreenDiff() {
        return greenDiff;
    }

    @Override
    public int getBlueDiff() {
        return blueDiff;
    }

}
