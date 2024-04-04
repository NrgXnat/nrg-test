package org.nrg.testing.xnat.performance.charting

import org.nrg.testing.xnat.performance.PerformanceUtils
import org.nrg.testing.xnat.performance.actions.SimpleTimedAction
import org.nrg.testing.xnat.performance.persistence.HistoricalPerformanceCatalog
import org.nrg.testing.xnat.performance.persistence.SimpleOverallTimeRecord

class ComparativeOverallTimeCharter extends SimpleOverallTimeCharter {

    @Override
    HistoricalPerformanceCatalog<SimpleOverallTimeRecord> readHistoryFor(SimpleTimedAction performanceWorkflow) {
        final HistoricalPerformanceCatalog<SimpleOverallTimeRecord> selfCatalog = PerformanceUtils.readHistory(performanceWorkflow.identifier)
        final Map<RequestedComparison, HistoricalPerformanceCatalog<SimpleOverallTimeRecord>> comparedCatalogs =
                performanceWorkflow.requestedComparisons.collectEntries { comparison ->
                    [(comparison) : PerformanceUtils.readHistory(comparison.otherTestId)]
                }

        final List<String> supportedVersions = selfCatalog.findVersionOverlapWithOtherCatalogs(comparedCatalogs.values())

        final HistoricalPerformanceCatalog<SimpleOverallTimeRecord> combinedHistory = new HistoricalPerformanceCatalog<>()
        selfCatalog.entries.findAll { entry ->
            if (entry.xnatVersion in supportedVersions) {
                entry.setChartGrouping('standard')
                combinedHistory.entries << entry
            }
        }
        comparedCatalogs.each { comparison, history ->
            history.entries.findAll { entry ->
                if (entry.xnatVersion in supportedVersions) {
                    entry.setChartGrouping(comparison.otherTestDescription)
                    combinedHistory.entries << entry
                }
            }
        }
        combinedHistory
    }

    @Override
    String remapIdentifier(SimpleTimedAction performanceWorkflow) {
        "${performanceWorkflow.identifier}-comparative"
    }

}
