package org.nrg.testing.xnat.performance.actions

import groovy.util.logging.Log4j
import org.nrg.testing.xnat.performance.PerformanceStateHelper
import org.nrg.testing.xnat.performance.persistence.ErrorProneAggregatableRecord
import org.nrg.testing.xnat.performance.validator.ErrorProneAggregatableValidator
import org.nrg.testing.xnat.performance.validator.PerformanceValidator
import org.nrg.xnat.interfaces.XnatInterface

import java.util.function.BiConsumer

@Log4j
class ErrorProneAggregatableAction implements
        CheckablePerformanceWorkflow<ErrorProneAggregatableAction, ErrorProneAggregatableRecord> {

    BiConsumer<XnatInterface, ActionAggregator> performanceTestAction

    ErrorProneAggregatableAction(String identifier) {
        setIdentifier(identifier)
    }

    ErrorProneAggregatableAction performanceTestAction(BiConsumer<XnatInterface, ActionAggregator> performanceTestAction) {
        setPerformanceTestAction(performanceTestAction)
        this
    }

    @Override
    ErrorProneAggregatableRecord produceCheckableEntry(PerformanceStateHelper stateHelper) {
        final ActionAggregator aggregator = new ActionAggregator()
        performanceTestAction.accept(stateHelper.interfaceFor(userProvider.nextUser()), aggregator)
        final long millis = aggregator.getTotalTime()
        log.info("Completed aggregatable actions of ${identifier} with ${aggregator.successCount} successes and ${aggregator.failureCount} failures...")
        new ErrorProneAggregatableRecord(
                successCount: aggregator.successCount,
                failureCount: aggregator.failureCount,
                overallTimeInMillis: millis
        )
    }

    @Override
    PerformanceValidator<ErrorProneAggregatableRecord> getDefaultValidator() {
        ErrorProneAggregatableValidator.DEFAULT
    }

}
