package org.nrg.testing.xnat.performance.charting

import org.nrg.testing.latex.LatexDocument
import org.nrg.testing.latex.SimpleBarGraph
import org.nrg.testing.xnat.performance.actions.SimpleTimedAction
import org.nrg.testing.xnat.performance.persistence.SimpleOverallTimeRecord

class SimpleOverallTimeCharter extends PerformanceCharter<SimpleTimedAction, SimpleOverallTimeRecord> {

    @Override
    LatexDocument produceDocument(SimpleTimedAction performanceWorkflow, List<SimpleOverallTimeRecord> historicalRecord) {
        final SimpleBarGraph barGraph = new SimpleBarGraph()
                .title(performanceWorkflow.title)
                .xlabel('XNAT version')
                .ylabel('Overall time in seconds')
        final Closure<String> convertAndDisplay = { long millis ->
            final double seconds = millis / 1000.0
            String.valueOf(seconds.round(seconds > 500 ? 0 : 1)) // dont display fractional seconds for large times
        }
        historicalRecord.each { record ->
            barGraph.addBar(record.xnatVersion, convertAndDisplay(record.overallTimeInMillis))
        }
        barGraph
    }

}
