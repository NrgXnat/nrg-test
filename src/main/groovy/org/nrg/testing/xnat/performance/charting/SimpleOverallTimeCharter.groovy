package org.nrg.testing.xnat.performance.charting

import org.nrg.testing.latex.BarGraph
import org.nrg.testing.latex.TrueBarGraph
import org.nrg.testing.xnat.performance.actions.SimpleTimedAction
import org.nrg.testing.xnat.performance.persistence.SimpleOverallTimeRecord

class SimpleOverallTimeCharter extends GenericBarGraphCharter<SimpleTimedAction, SimpleOverallTimeRecord> {

    @Override
    BarGraph getBaseBarGraph() {
        new TrueBarGraph()
    }

    @Override
    String getPointMetaFrom(SimpleOverallTimeRecord simpleOverallTimeRecord) {
        null
    }

}
