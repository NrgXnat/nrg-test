package org.nrg.testing.xnat.performance.charting

import org.nrg.testing.latex.DataPoint
import org.nrg.testing.latex.LatexDocument
import org.nrg.testing.latex.BarGraph
import org.nrg.testing.xnat.performance.actions.CheckablePerformanceWorkflow
import org.nrg.testing.xnat.performance.persistence.OverallTimeRecord

abstract class GenericBarGraphCharter<
        T extends CheckablePerformanceWorkflow<T, V>,
        V extends OverallTimeRecord<V>> extends PerformanceCharter<T, V> {

    abstract BarGraph getBaseBarGraph()

    abstract String getPointMetaFrom(V record)

    @Override
    LatexDocument produceDocument(T performanceWorkflow, List<V> historicalRecord) {
        if (historicalRecord.size() == 0) {
            return null
        }

        final BarGraph barGraph = getBaseBarGraph()
                .title(performanceWorkflow.title)
                .ylabel('XNAT version')
                .xlabel(getXLabel())
        historicalRecord.each { record ->
            barGraph.datasetFor(record.chartGrouping).dataPoints << new DataPoint()
                    .x(convertAndDisplay(extractTimeFromRecord(record)))
                    .y(record.xnatVersion)
                    .pointMeta(getPointMetaFrom(record))
        }

        barGraph
    }

    String getXLabel() {
        'Overall time in seconds'
    }

    long extractTimeFromRecord(V record) {
        record.overallTimeInMillis
    }

    protected String convertAndDisplay(long millis) {
        final double seconds = millis / 1000.0
        String.valueOf(seconds.round(seconds > 500 ? 0 : 1)) // dont display fractional seconds for large times
    }

}
