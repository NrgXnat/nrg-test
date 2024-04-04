package org.nrg.testing.xnat.performance.charting

import org.nrg.testing.latex.BarGraph
import org.nrg.testing.latex.ColorBarGraph
import org.nrg.testing.xnat.performance.actions.ErrorProneAggregatableAction
import org.nrg.testing.xnat.performance.persistence.ErrorProneAggregatableRecord

class ErrorProneActionTimeCharter extends GenericBarGraphCharter<ErrorProneAggregatableAction, ErrorProneAggregatableRecord> {

    @Override
    BarGraph getBaseBarGraph() {
        new ColorBarGraph()
    }

    @Override
    String getPointMetaFrom(ErrorProneAggregatableRecord record) {
        (100 * (record.successCount / (record.failureCount + record.successCount))).round(1).toPlainString()
    }

}
