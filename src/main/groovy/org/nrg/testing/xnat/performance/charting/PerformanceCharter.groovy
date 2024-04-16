package org.nrg.testing.xnat.performance.charting

import groovy.util.logging.Log4j
import org.nrg.testing.latex.LatexDocument
import org.nrg.testing.xnat.conf.Settings
import org.nrg.testing.xnat.performance.PerformanceUtils
import org.nrg.testing.xnat.performance.actions.CheckablePerformanceWorkflow
import org.nrg.testing.xnat.performance.actions.SimpleTimedAction
import org.nrg.testing.xnat.performance.persistence.CheckablePerformanceEntry
import org.nrg.testing.xnat.performance.persistence.HistoricalPerformanceCatalog
import org.nrg.testing.xnat.performance.persistence.SimpleOverallTimeRecord

@Log4j
abstract class PerformanceCharter<
        T extends CheckablePerformanceWorkflow<T, V>,
        V extends CheckablePerformanceEntry<V>> {

    private static final int MAX_COMPILE_DURATION_MILLIS = 10000

    void chart(T performanceWorkflow) {
        final T transformedWorkflow = transformPerformanceWorkflow(performanceWorkflow)
        final HistoricalPerformanceCatalog<V> history = readHistoryFor(transformedWorkflow)
        final LatexDocument chart = produceDocument(transformedWorkflow, history.entries)
        if (chart) {
            final File chartFile = PerformanceUtils.chartFor(remapIdentifier(transformedWorkflow))
            chartFile.text = chart.produceSourceDocument()
            if (Settings.PERFORMANCE_COMPILE_PDF) {
                log.info('Attempting to compile result to PDF...')
                2.times {
                    final StringBuilder stdOut = new StringBuilder()
                    final StringBuilder stdErr = new StringBuilder()
                    final Process process = "pdflatex -output-directory=${PerformanceUtils.PERFORMANCE_SUBDIR.toAbsolutePath()} ${chartFile.toPath().toAbsolutePath()}".execute()
                    process.consumeProcessOutput(stdOut, stdErr)
                    process.waitForOrKill(MAX_COMPILE_DURATION_MILLIS)
                    log.info(stdOut)
                    log.warn(stdErr)
                }
            }
        }
    }

    abstract LatexDocument produceDocument(T performanceWorkflow, List<V> historicalRecord)

    List<PerformanceCharter<T, V>> deriveComparativeCharters(T performanceWorkflow, List<RequestedComparison> requestedComparisons) {
        []
    }

    HistoricalPerformanceCatalog<V> readHistoryFor(T performanceWorkflow) {
        PerformanceUtils.readHistory(performanceWorkflow.identifier)
    }

    HistoricalPerformanceCatalog<V> readAndCombineComparedHistoryFor(T performanceWorkflow) {
        final HistoricalPerformanceCatalog<V> selfCatalog = PerformanceUtils.readHistory(performanceWorkflow.identifier)
        final Map<RequestedComparison, HistoricalPerformanceCatalog<V>> comparedCatalogs =
                performanceWorkflow.requestedComparisons.collectEntries { comparison ->
                    [(comparison) : PerformanceUtils.readHistory(comparison.otherTestId)]
                }

        final List<String> supportedVersions = selfCatalog.findVersionOverlapWithOtherCatalogs(comparedCatalogs.values())

        final HistoricalPerformanceCatalog<V> combinedHistory = new HistoricalPerformanceCatalog<>()
        selfCatalog.entries.findAll { entry ->
            if (entry.xnatVersion in supportedVersions) {
                entry.setChartGrouping('standard')
                combinedHistory.entries << entry
            }
        }
        comparedCatalogs.each { comparison, history ->
            history.entries.findAll { entry ->
                if (entry.xnatVersion in supportedVersions) {
                    entry.setChartGrouping(comparison.otherTestDescription)
                    combinedHistory.entries << entry
                }
            }
        }
        combinedHistory
    }

    T transformPerformanceWorkflow(T performanceWorkflow) {
        performanceWorkflow
    }

    String remapIdentifier(T performanceWorkflow) {
        performanceWorkflow.identifier
    }

}
