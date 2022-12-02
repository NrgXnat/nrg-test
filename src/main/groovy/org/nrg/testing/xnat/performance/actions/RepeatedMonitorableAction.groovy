package org.nrg.testing.xnat.performance.actions

import groovy.util.logging.Log4j
import org.nrg.testing.xnat.performance.PerformanceStateHelper
import org.nrg.testing.xnat.performance.persistence.CumulativeTimeSeriesData
import org.nrg.testing.xnat.performance.validator.PerformanceValidator
import org.nrg.xnat.interfaces.XnatInterface

import java.util.function.Consumer

@Log4j
class RepeatedMonitorableAction implements
        CheckablePerformanceWorkflow<RepeatedMonitorableAction, CumulativeTimeSeriesData> {

    int overallIterationCount
    int actionsPerSnapshot = 10
    Consumer<XnatInterface> performanceTestAction

    RepeatedMonitorableAction(String identifier) {
        setIdentifier(identifier)
    }

    RepeatedMonitorableAction overallIterationCount(int iterCount) {
        setOverallIterationCount(iterCount)
        this
    }

    RepeatedMonitorableAction actionsPerSnapshot(int actions) {
        setActionsPerSnapshot(actions)
        this
    }

    RepeatedMonitorableAction performanceTestAction(Consumer<XnatInterface> performanceTestAction) {
        setPerformanceTestAction(performanceTestAction)
        this
    }

    @Override
    CumulativeTimeSeriesData produceCheckableEntry(PerformanceStateHelper stateHelper) {
        final RepeatedActionMonitor monitor = new RepeatedActionMonitor(actionsPerSnapshot)
        int currentIterationCount = 0
        monitor.start()
        while (currentIterationCount < overallIterationCount) {
            actionsPerSnapshot.times {
                performanceTestAction.accept(stateHelper.interfaceFor(userProvider.nextUser()))
            }
            currentIterationCount += actionsPerSnapshot
            monitor.split()
            log.info("Completed ${currentIterationCount} total invocations of the repeated action identified by ${identifier}...")
        }
        new CumulativeTimeSeriesData(timeSeriesData: monitor.splitsInMillis)
    }

    @Override
    PerformanceValidator<CumulativeTimeSeriesData> getDefaultValidator() {
        throw new UnsupportedOperationException('This type of action does not support a default validator. Please specify one.')
    }

}
