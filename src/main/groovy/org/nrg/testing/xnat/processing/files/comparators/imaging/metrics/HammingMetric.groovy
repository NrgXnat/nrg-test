package org.nrg.testing.xnat.processing.files.comparators.imaging.metrics

import org.nrg.testing.xnat.processing.files.comparators.imaging.ComparisonPixel

class HammingMetric extends DiscreteMetric {

    @Override
    double distance(ComparisonPixel pixel) {
        ((pixel.isColor) ? [pixel.redDiff, pixel.greenDiff, pixel.blueDiff] : [pixel.grayDiff]).sum { diff ->
            (diff == 0) ? 0 : 1
        } as double
    }

}
