package org.nrg.testing.xnat.processing.files.comparators.imaging;

import org.nrg.testing.xnat.processing.files.comparators.imaging.metrics.Metric;

/**
 * Used when generated and source pixels are identical to speed up metric calculations: d(x, x) = 0
 */
public class ZeroComparisonPixel extends ComparisonPixel {

    public ZeroComparisonPixel(int sourceRed, int sourceGreen, int sourceBlue) {
        super(sourceRed, sourceGreen, sourceBlue, sourceRed, sourceGreen, sourceBlue);
    }

    public ZeroComparisonPixel(int sourceGray) {
        super(sourceGray, sourceGray);
    }

    @Override
    public double calculateDistance(Metric metric) {
        return 0;
    }

}
