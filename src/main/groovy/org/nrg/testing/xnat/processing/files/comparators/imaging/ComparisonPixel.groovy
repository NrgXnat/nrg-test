package org.nrg.testing.xnat.processing.files.comparators.imaging

import groovy.transform.EqualsAndHashCode
import org.nrg.testing.xnat.processing.files.comparators.imaging.metrics.Metric

@EqualsAndHashCode
class ComparisonPixel {

    int sourceRed, sourceGreen, sourceBlue, sourceGray
    int generatedRed, generatedGreen, generatedBlue, generatedGray
    boolean isColor
    int x, y, z

    ComparisonPixel(int sourceRed, int sourceGreen, int sourceBlue, int generatedRed, int generatedGreen, int generatedBlue) {
        this.sourceRed = sourceRed
        this.sourceGreen = sourceGreen
        this.sourceBlue = sourceBlue
        this.generatedRed = generatedRed
        this.generatedGreen = generatedGreen
        this.generatedBlue = generatedBlue
        isColor = true
    }

    ComparisonPixel(int sourceGray, int generatedGray) {
        this.sourceGray = sourceGray
        this.generatedGray = generatedGray
        isColor = false
    }

    protected ComparisonPixel() {}

    void setCoordinates(int page, int x, int y) {
        z = page
        this.x = x
        this.y = y
    }

    int getGrayDiff() {
        sourceGray - generatedGray
    }

    int getRedDiff() {
        sourceRed - generatedRed
    }

    int getGreenDiff() {
        sourceGreen - generatedGreen
    }

    int getBlueDiff() {
        sourceBlue - generatedBlue
    }

    double calculateDistance(Metric metric) {
        isTrivial() ? 0 : metric.distance(this)
    }

    boolean isAdjacent(ComparisonPixel other) {
        Math.abs(x - other.x) + Math.abs(y - other.y) == 1 // two coordinates are adjacent in lattice iff they're a taxicab distance of 1 apart
    }

    // i.e. pixels are identical
    boolean isTrivial() {
        (isColor) ? (redDiff == 0 && greenDiff == 0 && blueDiff == 0) : (grayDiff == 0)
    }

    @Override
    String toString() {
        "ComparisonPixel pair at (${x}, ${y}) on slice ${z}"
    }

    PixelValue getGeneratedPixelValue() {
        return isColor ? new PixelValue(generatedRed, generatedGreen, generatedBlue) : new PixelValue(generatedGray)
    }

    PixelValue getSourcePixelValue() {
        return isColor ? new PixelValue(sourceRed, sourceGreen, sourceBlue) : new PixelValue(sourceGray)
    }

}
