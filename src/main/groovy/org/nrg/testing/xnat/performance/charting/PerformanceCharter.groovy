package org.nrg.testing.xnat.performance.charting

import groovy.util.logging.Log4j
import org.nrg.testing.latex.LatexDocument
import org.nrg.testing.xnat.conf.Settings
import org.nrg.testing.xnat.performance.PerformanceUtils
import org.nrg.testing.xnat.performance.actions.CheckablePerformanceWorkflow
import org.nrg.testing.xnat.performance.persistence.CheckablePerformanceEntry
import org.nrg.testing.xnat.performance.persistence.HistoricalPerformanceCatalog

@Log4j
abstract class PerformanceCharter<
        T extends CheckablePerformanceWorkflow<T, V>,
        V extends CheckablePerformanceEntry<V>> {

    private static final int MAX_COMPILE_DURATION_MILLIS = 10000

    void chart(T performanceWorkflow) {
        final HistoricalPerformanceCatalog<V> history = PerformanceUtils.readHistory(performanceWorkflow.identifier)
        final LatexDocument chart = produceDocument(performanceWorkflow, history.entries)
        if (chart) {
            final File chartFile = PerformanceUtils.chartFor(performanceWorkflow.identifier)
            chartFile.text = chart.produceSourceDocument()
            if (Settings.PERFORMANCE_COMPILE_PDF) {
                log.info('Attempting to compile result to PDF...')
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

    abstract LatexDocument produceDocument(T performanceWorkflow, List<V> historicalRecord)

}
