package org.nrg.testing.xnat.processing.files.comparators.imaging;

import org.nrg.testing.xnat.processing.files.comparators.imaging.metrics.Metric;

import static java.lang.Math.*;

public class PNormDistance extends Metric {

    private double p;

    public PNormDistance(double p) {
        this.p = p;
    }

    @Override
    public double distance(ComparisonPixel pixel) {
        if (pixel.isColor) {
            return pow(pow(abs(pixel.getRedDiff()), p) + pow(abs(pixel.getGreenDiff()), p) + pow(abs(pixel.getBlueDiff()), p), 1/p);
        } else {
            return abs(pixel.getGrayDiff()); // p-norm reduces to absolute value over R
        }
    }
}
