package org.nrg.testing.xnat.performance.charting

import org.nrg.testing.latex.LatexDocument
import org.nrg.testing.xnat.performance.PerformanceUtils
import org.nrg.testing.xnat.performance.actions.CheckablePerformanceWorkflow
import org.nrg.testing.xnat.performance.persistence.CheckablePerformanceEntry
import org.nrg.testing.xnat.performance.persistence.HistoricalPerformanceCatalog

abstract class PerformanceCharter<
        T extends CheckablePerformanceWorkflow<T, V>,
        V extends CheckablePerformanceEntry<V>> {

    void chart(T performanceWorkflow) {
        final HistoricalPerformanceCatalog<V> history = PerformanceUtils.readHistory(performanceWorkflow.identifier)
        final LatexDocument chart = produceDocument(performanceWorkflow, history.entries)
        if (chart) {
            PerformanceUtils.chartFor(performanceWorkflow.identifier).text = chart.produceSourceDocument()
        }
    }

    abstract LatexDocument produceDocument(T performanceWorkflow, List<V> historicalRecord)

}
