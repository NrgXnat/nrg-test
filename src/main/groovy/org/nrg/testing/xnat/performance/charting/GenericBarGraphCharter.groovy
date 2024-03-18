package org.nrg.testing.xnat.performance.charting

import org.nrg.testing.latex.Bar
import org.nrg.testing.latex.LatexDocument
import org.nrg.testing.latex.BarGraph
import org.nrg.testing.xnat.performance.actions.CheckablePerformanceWorkflow
import org.nrg.testing.xnat.performance.persistence.OverallTimeRecord

abstract class GenericBarGraphCharter<
        T extends CheckablePerformanceWorkflow<T, V>,
        V extends OverallTimeRecord<V>> extends PerformanceCharter<T, V> {

    abstract BarGraph getBaseBarGraph()

    abstract Bar generateBarFrom(V record, List<V> allRecords)

    @Override
    LatexDocument produceDocument(T performanceWorkflow, List<V> historicalRecord) {
        final BarGraph barGraph = getBaseBarGraph()
                .title(performanceWorkflow.title)
                .ylabel('XNAT version')
                .xlabel('Overall time in seconds')
        historicalRecord.each { record ->
            barGraph.bars << generateBarFrom(record, historicalRecord)
        }
        barGraph
    }

    protected String convertAndDisplay(long millis) {
        final double seconds = millis / 1000.0
        String.valueOf(seconds.round(seconds > 500 ? 0 : 1)) // dont display fractional seconds for large times
    }

}
