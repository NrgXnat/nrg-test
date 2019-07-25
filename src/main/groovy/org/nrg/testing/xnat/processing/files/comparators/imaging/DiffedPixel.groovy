package org.nrg.testing.xnat.processing.files.comparators.imaging

class DiffedPixel extends ComparisonPixel {

    int redDiff
    int greenDiff
    int blueDiff
    int grayDiff

    DiffedPixel(int sourceRed, int sourceGreen, int sourceBlue, int generatedRed, int generatedGreen, int generatedBlue) {
        redDiff = sourceRed - generatedRed
        greenDiff = sourceGreen - generatedGreen
        blueDiff = sourceBlue - generatedBlue
        isColor = true
    }

    DiffedPixel(int sourceGray, int generatedGray) {
        grayDiff = sourceGray - generatedGray
        isColor = false
    }

    protected DiffedPixel() {}

    @Override
    int getGrayDiff() {
        grayDiff
    }

    @Override
    int getRedDiff() {
        redDiff
    }

    @Override
    int getGreenDiff() {
        greenDiff
    }

    @Override
    int getBlueDiff() {
        blueDiff
    }

}
