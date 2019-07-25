package org.nrg.testing.xnat.processing.files.comparators.imaging.metrics

import org.nrg.testing.xnat.processing.files.comparators.imaging.ComparisonPixel

class DiscreteMetric extends Metric {

    @Override
    double distance(ComparisonPixel pixel) {
        pixel.isTrivial() ? 0 : 1
    }

}
