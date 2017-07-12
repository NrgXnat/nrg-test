package org.nrg.testing.xnat.processing.files.comparators.imaging.metrics;

import org.nrg.testing.xnat.processing.files.comparators.imaging.ComparisonPixel;

public class HammingMetric extends DiscreteMetric {

    @Override
    public double distance(ComparisonPixel pixel) {
        if (pixel.isColor()) {
            int diffs = 0;
            if (pixel.getRedDiff()   != 0) diffs++;
            if (pixel.getGreenDiff() != 0) diffs++;
            if (pixel.getBlueDiff()  != 0) diffs++;
            return diffs;
        } else {
            return super.distance(pixel); // HammingMetric reduces to the discrete metric over R
        }
    }

}
