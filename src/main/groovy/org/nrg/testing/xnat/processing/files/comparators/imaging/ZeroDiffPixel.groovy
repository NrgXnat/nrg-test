package org.nrg.testing.xnat.processing.files.comparators.imaging

import org.nrg.testing.xnat.processing.files.comparators.imaging.metrics.Metric

/**
 * Used when generated and source pixels are identical to speed up metric calculations: d(x, x) = 0. Also, when using DiffedPixel instead of base ComparisonPixel,
 * we only store the differences. Hence, we can create one instance of this object in PixelFactory and reuse to cut down on memory consumption.
 */
class ZeroDiffPixel extends DiffedPixel {

    ZeroDiffPixel(boolean isColor) {
        if (isColor) {
            redDiff = 0
            greenDiff = 0
            blueDiff = 0
        } else {
            grayDiff = 0
        }
        this.isColor = isColor
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
