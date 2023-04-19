package org.nrg.testing.xnat.performance.validator

import groovy.util.logging.Log4j
import org.nrg.testing.xnat.conf.Settings
import org.nrg.testing.xnat.performance.CheckablePerformanceResult
import org.nrg.testing.xnat.performance.PerformanceUtils
import org.nrg.testing.xnat.performance.persistence.CheckablePerformanceEntry
import org.nrg.testing.xnat.performance.persistence.HistoricalPerformanceCatalog
import org.nrg.xnat.interfaces.XnatInterface

@Log4j
abstract class PerformanceValidator<X extends CheckablePerformanceEntry<X>> {

    CheckablePerformanceResult validate(String identifier, X observedBehavior) {
        final HistoricalPerformanceCatalog<X> historicalRecord = PerformanceUtils.readHistory(identifier)
        final CheckablePerformanceResult result = validateAgainst(historicalRecord.findBaseline(), observedBehavior)
        if (Settings.PERFORMANCE_SET_BASELINES) {
            historicalRecord.entries.each { entry ->
                entry.setBaseline(false)
            }
            observedBehavior.setBaseline(true)
        }
        historicalRecord.entries << observedBehavior
        PerformanceUtils.writeHistory(identifier, historicalRecord)
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