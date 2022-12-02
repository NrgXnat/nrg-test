package org.nrg.testing.xnat.performance.validator

import groovy.util.logging.Log4j
import org.nrg.testing.xnat.performance.CheckablePerformanceResult
import org.nrg.testing.xnat.performance.persistence.ErrorProneAggregatableRecord
import org.nrg.testing.xnat.performance.persistence.SimpleOverallTimeRecord

@Log4j
class SimpleOverallTimeValidator implements
        PerformanceValidator<SimpleOverallTimeRecord>,
        OverallTimeAwareValidator<SimpleOverallTimeValidator> {

    public static final SimpleOverallTimeValidator DEFAULT = new SimpleOverallTimeValidator()

    @Override
    CheckablePerformanceResult validateAgainst(SimpleOverallTimeRecord previousRecord, SimpleOverallTimeRecord observedBehavior) {
        previousRecord != null ? checkOverallTime(previousRecord.overallTimeInMillis, observedBehavior.overallTimeInMillis) : CheckablePerformanceResult.SUCCESS
    }

}
