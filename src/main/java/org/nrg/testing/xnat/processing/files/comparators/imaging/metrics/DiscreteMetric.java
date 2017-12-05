package org.nrg.testing.xnat.processing.files.comparators.imaging.metrics;

import org.nrg.testing.xnat.processing.files.comparators.imaging.ComparisonPixel;

public class DiscreteMetric extends Metric {

    @Override
    public double distance(ComparisonPixel pixel) {
        return 1; // ZeroComparisonPixel should always return 0 on a metric without even getting this far
    }

}
