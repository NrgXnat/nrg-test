package org.nrg.testing.xnat.performance.persistence

import org.apache.commons.math3.ml.distance.EuclideanDistance

import java.util.function.Function

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

}
