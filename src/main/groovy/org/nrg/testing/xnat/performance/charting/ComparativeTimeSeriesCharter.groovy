package org.nrg.testing.xnat.performance.charting

import org.nrg.testing.xnat.performance.actions.RepeatedMonitorableAction
import org.nrg.testing.xnat.performance.persistence.CumulativeTimeSeriesData
import org.nrg.testing.xnat.performance.persistence.HistoricalPerformanceCatalog

class ComparativeTimeSeriesCharter extends CumulativeTimeSeriesCharter {

    String version
    String otherTestId
    String otherTestDescription
    HistoricalPerformanceCatalog<CumulativeTimeSeriesData> cachedHistory

    @Override
    HistoricalPerformanceCatalog<CumulativeTimeSeriesData> readHistoryFor(RepeatedMonitorableAction performanceWorkflow) {
        cachedHistory
    }

    @Override
    RepeatedMonitorableAction transformPerformanceWorkflow(RepeatedMonitorableAction performanceWorkflow) {
        new RepeatedMonitorableAction("comparative-${performanceWorkflow.identifier}-${otherTestId}-${version}")
            .title("${performanceWorkflow.title} (baseline compared to ${otherTestDescription})")
            .actionDescription(performanceWorkflow.actionDescription)
    }

}
