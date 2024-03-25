package org.nrg.testing.xnat.performance.actions

import groovy.util.logging.Log4j
import org.nrg.testing.xnat.performance.PerformanceStateHelper
import org.nrg.testing.xnat.performance.PerformanceUtils
import org.nrg.testing.xnat.performance.charting.ComparativeTimeSeriesCharter
import org.nrg.testing.xnat.performance.charting.CumulativeTimeSeriesCharter
import org.nrg.testing.xnat.performance.charting.PerformanceCharter
import org.nrg.testing.xnat.performance.persistence.CumulativeTimeSeriesData
import org.nrg.testing.xnat.performance.persistence.HistoricalPerformanceCatalog
import org.nrg.testing.xnat.performance.validator.PerformanceValidator
import org.nrg.xnat.interfaces.XnatInterface

import java.util.function.Consumer

@Log4j
/**
 * RepeatedMonitorableAction defines a measurable action that can be repeated several
 * times and measured at a fixed interval (by iteration count). The action defined in
 * {@link #performanceTestAction() performanceTestAction} and total number of iterations
 * settable by {@link #overallIterationCount(int) overallIterationCount} are required,
 * while the number of iterations captured in an individual snapshot is optional,
 * configurable with {@link #actionsPerSnapshot(int) actionsPerSnapshot}.
 *
 * Examples of intended use cases include:
 * <ul>
 *     <li> create a project in XNAT 1000 times, measuring the time taken every 10 projects
 *     <li> load an expensive stored search 5 times in a row, measuring the time on each load
 * </ul>
 *
 * This action does not support a default validator, so it must
 * be specified using {@link #validateUsing()}.
 */
class RepeatedMonitorableAction implements
        CheckablePerformanceWorkflow<RepeatedMonitorableAction, CumulativeTimeSeriesData> {

    int overallIterationCount
    int actionsPerSnapshot = 10
    Consumer<XnatInterface> performanceTestAction
    String actionDescription
    List<RequestedComparison> requestedComparisons = []

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

    RepeatedMonitorableAction actionDescription(String actionDescription) {
        setActionDescription(actionDescription)
        this
    }

    RepeatedMonitorableAction compareTo(String otherTestId, String otherTestDescription, String otherTestShortKey) {
        requestedComparisons << new RequestedComparison(
                otherTestId: otherTestId,
                otherTestDescription: otherTestDescription,
                otherTestShortKey: otherTestShortKey
        )
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

    @Override
    PerformanceCharter<RepeatedMonitorableAction, CumulativeTimeSeriesData> getPerformanceCharter() {
        new CumulativeTimeSeriesCharter()
    }

    @Override
    List<PerformanceCharter<RepeatedMonitorableAction, CumulativeTimeSeriesData>> getComparativeCharters() {
        final HistoricalPerformanceCatalog<CumulativeTimeSeriesData> selfCatalog = PerformanceUtils.readHistory(identifier)
        final List<String> selfCatalogVersions = selfCatalog.entries*.xnatVersion

        requestedComparisons.collectMany { requestedComparison ->
            final HistoricalPerformanceCatalog<CumulativeTimeSeriesData> otherCatalog = PerformanceUtils.readHistory(requestedComparison.otherTestId)
            otherCatalog.entries*.xnatVersion.findAll { otherVersion ->
                otherVersion in selfCatalogVersions
            }.collect { version ->
                new ComparativeTimeSeriesCharter(
                        version: version,
                        otherTestId: requestedComparison.otherTestId,
                        otherTestDescription: requestedComparison.otherTestDescription,
                        cachedHistory: new HistoricalPerformanceCatalog<CumulativeTimeSeriesData>(entries: [
                                selfCatalog.lookupEntryByVersion(version),
                                new CumulativeTimeSeriesData(
                                        timeSeriesData: otherCatalog.lookupEntryByVersion(version).timeSeriesData,
                                        xnatVersion: "${version}-${requestedComparison.otherTestShortKey}"
                                )
                        ])
                )
            }
        }
    }

    private class RequestedComparison {
        String otherTestId
        String otherTestDescription
        String otherTestShortKey
    }

}
