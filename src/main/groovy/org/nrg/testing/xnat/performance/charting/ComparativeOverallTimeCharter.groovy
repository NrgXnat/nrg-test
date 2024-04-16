package org.nrg.testing.xnat.performance.charting

import org.nrg.testing.xnat.performance.actions.SimpleTimedAction
import org.nrg.testing.xnat.performance.persistence.HistoricalPerformanceCatalog
import org.nrg.testing.xnat.performance.persistence.SimpleOverallTimeRecord

class ComparativeOverallTimeCharter extends SimpleOverallTimeCharter {

    @Override
    HistoricalPerformanceCatalog<SimpleOverallTimeRecord> readHistoryFor(SimpleTimedAction performanceWorkflow) {
        readAndCombineComparedHistoryFor(performanceWorkflow)
    }

    @Override
    String remapIdentifier(SimpleTimedAction performanceWorkflow) {
        "${performanceWorkflow.identifier}-comparative"
    }

}
