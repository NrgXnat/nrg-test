package org.nrg.testing.xnat.performance.actions

import groovy.util.logging.Log4j
import org.nrg.testing.xnat.performance.PerformanceStateHelper
import org.nrg.testing.xnat.performance.persistence.SimpleOverallTimeRecord
import org.nrg.testing.xnat.performance.validator.PerformanceValidator
import org.nrg.testing.xnat.performance.validator.SimpleOverallTimeValidator
import org.nrg.xnat.interfaces.XnatInterface

import java.util.function.BiConsumer

@Log4j
class SimpleTimedAction implements
        CheckablePerformanceWorkflow<SimpleTimedAction, SimpleOverallTimeRecord> {

    BiConsumer<XnatInterface, ActionMonitor> performanceTestAction

    SimpleTimedAction(String identifier) {
        setIdentifier(identifier)
    }

    SimpleTimedAction performanceTestAction(BiConsumer<XnatInterface, ActionMonitor> performanceTestAction) {
        setPerformanceTestAction(performanceTestAction)
        this
    }

    @Override
    SimpleOverallTimeRecord produceCheckableEntry(PerformanceStateHelper stateHelper) {
        final ActionMonitor monitor = new ActionMonitor()
        performanceTestAction.accept(stateHelper.interfaceFor(userProvider.nextUser()), monitor)
        final long millis = monitor.getTotalTime()
        log.info("Actions for ${identifier} completed in ${millis} ms...")
        new SimpleOverallTimeRecord(overallTimeInMillis: millis)
    }

    @Override
    PerformanceValidator<SimpleOverallTimeRecord> getDefaultValidator() {
        SimpleOverallTimeValidator.DEFAULT
    }

}
