package org.nrg.testing.xnat.performance.charting

import org.nrg.testing.latex.Bar
import org.nrg.testing.latex.BarGraph
import org.nrg.testing.xnat.performance.actions.SimpleTimedAction
import org.nrg.testing.xnat.performance.persistence.SimpleOverallTimeRecord

class SimpleOverallTimeCharter extends GenericBarGraphCharter<SimpleTimedAction, SimpleOverallTimeRecord> {

    @Override
    BarGraph getBaseBarGraph() {
        new BarGraph().additionalPlotOptions(['nodes near coords', 'nodes near coords align = horizontal'])
    }

    @Override
    Bar generateBarFrom(SimpleOverallTimeRecord record, List<SimpleOverallTimeRecord> ignored) {
        new Bar().label(record.xnatVersion).value(convertAndDisplay(record.overallTimeInMillis))
    }

}
