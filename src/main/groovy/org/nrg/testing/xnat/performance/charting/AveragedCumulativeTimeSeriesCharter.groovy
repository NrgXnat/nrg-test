package org.nrg.testing.xnat.performance.charting

import org.nrg.testing.latex.BarGraph
import org.nrg.testing.latex.TrueBarGraph
import org.nrg.testing.xnat.performance.actions.RepeatedMonitorableAction
import org.nrg.testing.xnat.performance.actions.SimpleTimedAction
import org.nrg.testing.xnat.performance.persistence.CumulativeTimeSeriesData
import org.nrg.testing.xnat.performance.persistence.HistoricalPerformanceCatalog

class AveragedCumulativeTimeSeriesCharter extends GenericBarGraphCharter<RepeatedMonitorableAction, CumulativeTimeSeriesData> {

    public static final AveragedCumulativeTimeSeriesCharter INSTANCE = new AveragedCumulativeTimeSeriesCharter()

    @Override
    BarGraph getBaseBarGraph() {
        new TrueBarGraph()
    }

    @Override
    String getPointMetaFrom(CumulativeTimeSeriesData cumulativeTimeSeriesData) {
        return null
    }

    @Override
    String getXLabel() {
        'Average time in seconds'
    }

    @Override
    long extractTimeFromRecord(CumulativeTimeSeriesData cumulativeTimeSeriesData) {
        (cumulativeTimeSeriesData.overallTimeInMillis /cumulativeTimeSeriesData.timeSeriesData.size()).round(0).longValue()
    }

    @Override
    List<PerformanceCharter<RepeatedMonitorableAction, CumulativeTimeSeriesData>> deriveComparativeCharters(RepeatedMonitorableAction performanceWorkflow, List<RequestedComparison> requestedComparisons) {
        [
                new AveragedCumulativeTimeSeriesCharter() {
                    @Override
                    String remapIdentifier(RepeatedMonitorableAction test) {
                        "comparative-${test.identifier}"
                    }

                    @Override
                    HistoricalPerformanceCatalog<CumulativeTimeSeriesData> readHistoryFor(RepeatedMonitorableAction test) {
                        readAndCombineComparedHistoryFor(test)
                    }
                }
        ]
    }

}
