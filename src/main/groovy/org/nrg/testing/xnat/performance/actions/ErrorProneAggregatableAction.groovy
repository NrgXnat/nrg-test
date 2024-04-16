package org.nrg.testing.xnat.performance.actions

import groovy.util.logging.Log4j
import org.nrg.testing.latex.LatexDocument
import org.nrg.testing.xnat.performance.PerformanceStateHelper
import org.nrg.testing.xnat.performance.charting.ErrorProneActionTimeCharter
import org.nrg.testing.xnat.performance.charting.PerformanceCharter
import org.nrg.testing.xnat.performance.persistence.ErrorProneAggregatableRecord
import org.nrg.testing.xnat.performance.validator.ErrorProneAggregatableValidator
import org.nrg.testing.xnat.performance.validator.PerformanceValidator
import org.nrg.xnat.interfaces.XnatInterface

import java.util.function.BiConsumer

@Log4j
/**
 * ErrorProneAggregatableAction defines a measurable action that is designed to be repeated
 * several times and measured in duration at the end, with the allowance that some of the
 * individual calls may fail. The action is defined with
 * {@link #performanceTestAction() performanceTestAction} and the provided instance of
 * {@link ActionAggregator} should be used to count individual success or failures.
 *
 * The main use case of this right now is for large-scale ingestion in XNAT,
 * where intermittent errors often occur.
 *
 * The default validator is {@link ErrorProneAggregatableValidator#DEFAULT}, but can changed
 * with {@link #validateUsing()}.
 */
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

    @Override
    PerformanceCharter<ErrorProneAggregatableAction, ErrorProneAggregatableRecord> getDefaultPerformanceCharter() {
        new ErrorProneActionTimeCharter()
    }

}
