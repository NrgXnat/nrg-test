package org.nrg.testing.xnat.processing.files.comparators.imaging

import org.nrg.testing.xnat.processing.files.comparators.imaging.metrics.Metric

/**
 * Used when generated and source pixels are identical to speed up metric calculations: d(x, x) = 0
 */
class ZeroComparisonPixel extends ComparisonPixel {

    ZeroComparisonPixel(int sourceRed, int sourceGreen, int sourceBlue) {
        super(sourceRed, sourceGreen, sourceBlue, sourceRed, sourceGreen, sourceBlue)
    }

    ZeroComparisonPixel(int sourceGray) {
        super(sourceGray, sourceGray)
    }

    @Override
    double calculateDistance(Metric metric) {
        0
    }

    @Override
    boolean isTrivial() {
        true
    }

}
