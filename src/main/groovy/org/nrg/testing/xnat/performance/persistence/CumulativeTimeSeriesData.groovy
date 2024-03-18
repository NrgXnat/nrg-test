package org.nrg.testing.xnat.performance.persistence

import static java.lang.Math.abs

class CumulativeTimeSeriesData implements CheckablePerformanceEntry<CumulativeTimeSeriesData> {

    Map<Integer, Long> timeSeriesData

    Double calculateNormalizedAverageAbsoluteValueDistance(CumulativeTimeSeriesData otherDataset) {
        final List<Long> dataset1 = timeSeriesData.values() as List<Long>
        final List<Long> dataset2 = otherDataset.timeSeriesData.values() as List<Long>

        100 * (0 ..< dataset1.size()).sum { index ->
            (abs(dataset1[index] - dataset2[index]) / (dataset1[index] + dataset2[index]))
        } / dataset1.size()
    }

}
