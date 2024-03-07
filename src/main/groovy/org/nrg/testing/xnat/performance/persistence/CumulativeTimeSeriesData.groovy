package org.nrg.testing.xnat.performance.persistence

import org.apache.commons.math3.ml.distance.EuclideanDistance

import java.util.function.Function

import static java.lang.Math.abs

class CumulativeTimeSeriesData implements CheckablePerformanceEntry<CumulativeTimeSeriesData> {

    Map<Integer, Long> timeSeriesData

    Double calculateNormalizedDistanceTo(CumulativeTimeSeriesData otherDataset) {
        final Function<CumulativeTimeSeriesData, double[]> toSeconds = { CumulativeTimeSeriesData dataset ->
            dataset.timeSeriesData.values().sort(false).collect { measurement ->
                measurement / 1000.0
            }
        }

        final double[] firstDataset = toSeconds.apply(this)
        final double[] secondDataset = toSeconds.apply(otherDataset)

        new EuclideanDistance().compute(firstDataset, secondDataset) / (firstDataset.size())
    }

    Double calculateNormalizedAverageAbsoluteValueDistance(CumulativeTimeSeriesData otherDataset) {
        final List<Long> dataset1 = timeSeriesData.values() as List<Long>
        final List<Long> dataset2 = otherDataset.timeSeriesData.values() as List<Long>

        (0 ..< dataset1.size()).sum { index ->
            (abs(dataset1[index] - dataset2[index]) / (dataset1[index] + dataset2[index]))
        } / dataset1.size()
    }

}
