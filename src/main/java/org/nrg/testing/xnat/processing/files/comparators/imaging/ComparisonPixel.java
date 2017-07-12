package org.nrg.testing.xnat.processing.files.comparators.imaging;

import org.nrg.testing.xnat.processing.files.comparators.imaging.metrics.Metric;

public class ComparisonPixel {

    protected int sourceRed, sourceGreen, sourceBlue, sourceGray;
    protected int generatedRed, generatedGreen, generatedBlue, generatedGray;
    protected boolean isColor;
    protected int x, y, z;

    public ComparisonPixel(int sourceRed, int sourceGreen, int sourceBlue, int generatedRed, int generatedGreen, int generatedBlue) {
        this.sourceRed = sourceRed;
        this.sourceGreen = sourceGreen;
        this.sourceBlue = sourceBlue;
        this.generatedRed = generatedRed;
        this.generatedGreen = generatedGreen;
        this.generatedBlue = generatedBlue;
        isColor = true;
    }

    public ComparisonPixel(int sourceGray, int generatedGray) {
        this.sourceGray = sourceGray;
        this.generatedGray = generatedGray;
        isColor = false;
    }

    protected ComparisonPixel() {}

    public void setCoordinates(int page, int x, int y) {
        z = page;
        this.x = x;
        this.y = y;
    }

    public int getPage() {
        return z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isColor() {
        return isColor;
    }

    public int getSourceRed() {
        return sourceRed;
    }

    public int getSourceGreen() {
        return sourceGreen;
    }

    public int getSourceBlue() {
        return sourceBlue;
    }

    public int getSourceGray() {
        return sourceGray;
    }

    public int getGeneratedRed() {
        return generatedRed;
    }

    public int getGeneratedGreen() {
        return generatedGreen;
    }

    public int getGeneratedBlue() {
        return generatedBlue;
    }

    public int getGeneratedGray() {
        return generatedGray;
    }

    public int getGrayDiff() {
        return sourceGray - generatedGray;
    }

    public int getRedDiff() {
        return sourceRed - generatedRed;
    }

    public int getGreenDiff() {
        return sourceGreen - generatedGreen;
    }

    public int getBlueDiff() {
        return sourceBlue - generatedBlue;
    }

    public double calculateDistance(Metric metric) {
        return metric.distance(this);
    }

    public boolean isAdjacent(ComparisonPixel other) {
        return (Math.abs(x - other.x) + Math.abs(y - other.y) == 1); // two coordinates are adjacent in lattice iff they're a taxicab distance of 1 apart
    }

}
