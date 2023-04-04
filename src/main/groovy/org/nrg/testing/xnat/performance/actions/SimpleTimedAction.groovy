package org.nrg.testing.xnat.performance.actions

import groovy.util.logging.Log4j
import org.nrg.testing.xnat.performance.PerformanceStateHelper
import org.nrg.testing.xnat.performance.persistence.SimpleOverallTimeRecord
import org.nrg.testing.xnat.performance.validator.PerformanceValidator
import org.nrg.testing.xnat.performance.validator.SimpleOverallTimeValidator
import org.nrg.xnat.interfaces.XnatInterface

import java.util.function.BiConsumer

@Log4j
/**
 * SimpleTimedAction is the most basic type of action to implement. It simply runs
 * the command provided by {@link #performanceTestAction() performanceTestAction}
 * a single time and records the overall execution time. The provided
 * {@link ActionMonitor} instance allows for ending the monitorable period early
 * or resetting it to allow for some unmeasured content within the function.
 *
 * Examples of intended use cases include:
 * <ul>
 *     <li> C-STORE a very large session to XNAT and measure the overall time
 *     <li> Rebuild and archive a large study from XNAT's prearchive and measure
 *     the overall time.
 * </ul>
 *
 * The default validator is {@link SimpleOverallTimeValidator#DEFAULT}, but can changed
 * with {@link #validateUsing()}.
 */
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
