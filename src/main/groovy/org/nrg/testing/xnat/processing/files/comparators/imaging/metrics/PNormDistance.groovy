package org.nrg.testing.xnat.processing.files.comparators.imaging.metrics

import org.nrg.testing.xnat.processing.files.comparators.imaging.ComparisonPixel

import static java.lang.Math.*

class PNormDistance extends Metric {

    double p

    PNormDistance(double p) {
        this.p = p
    }

    @Override
    double distance(ComparisonPixel pixel) {
        final List<Integer> diffs = (pixel.isColor) ? [pixel.redDiff, pixel.greenDiff, pixel.blueDiff] : [pixel.grayDiff]
        pow(diffs.sum { pow(abs(it as int), p) } as double, 1/p)
    }

}
