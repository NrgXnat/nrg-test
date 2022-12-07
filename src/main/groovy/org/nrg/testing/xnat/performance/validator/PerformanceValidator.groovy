package org.nrg.testing.xnat.performance.validator

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.util.logging.Log4j
import org.nrg.testing.FileIOUtils
import org.nrg.testing.xnat.conf.Settings
import org.nrg.testing.xnat.performance.CheckablePerformanceResult
import org.nrg.testing.xnat.performance.persistence.CheckablePerformanceEntry
import org.nrg.testing.xnat.performance.persistence.HistoricalPerformanceCatalog
import org.nrg.xnat.interfaces.XnatInterface

import java.nio.file.Path
import java.nio.file.Paths

@Log4j
trait PerformanceValidator<X extends CheckablePerformanceEntry<X>> {

    public static final String DIR = 'performance'

    CheckablePerformanceResult validate(String identifier, X observedBehavior) {
        final ObjectMapper objectMapper = XnatInterface.XNAT_REST_MAPPER
        final Path performanceSubdir = Paths.get(Settings.DATA_LOCATION, DIR)
        FileIOUtils.mkdirs(performanceSubdir)
        final File historicalRecordFile = performanceSubdir.resolve("${identifier}.json").toFile()
        final HistoricalPerformanceCatalog<X> historicalRecord = historicalRecordFile.exists() ?
                objectMapper.readValue(historicalRecordFile, HistoricalPerformanceCatalog<X>) as HistoricalPerformanceCatalog<X> :
                new HistoricalPerformanceCatalog<X>()
        final CheckablePerformanceResult result = validateAgainst(historicalRecord.findBaseline(), observedBehavior)
        if (Settings.PERFORMANCE_SET_BASELINES) {
            historicalRecord.entries.each { entry ->
                entry.setBaseline(false)
            }
            observedBehavior.setBaseline(true)
        }
        historicalRecord.entries << observedBehavior
        objectMapper.writeValue(historicalRecordFile, historicalRecord)
        if (result.passed) {
            log.info("All checks passing for ${identifier}")
        } else {
            log.warn("Checks failed for ${identifier}")
        }
        log.info("Data for ${identifier}: ${XnatInterface.XNAT_REST_MAPPER.writeValueAsString(historicalRecord)}")
        result
    }

    abstract CheckablePerformanceResult validateAgainst(X previousRecord, X observedBehavior)

    String formatRateAsPercent(double value) {
        (100 * value).round(2) + '%'
    }

}