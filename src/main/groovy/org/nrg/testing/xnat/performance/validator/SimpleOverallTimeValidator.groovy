package org.nrg.testing.xnat.performance.validator

import groovy.util.logging.Log4j
import org.nrg.testing.xnat.performance.CheckablePerformanceResult
import org.nrg.testing.xnat.performance.persistence.ErrorProneAggregatableRecord
import org.nrg.testing.xnat.performance.persistence.SimpleOverallTimeRecord

@Log4j
/**
 * SimpleOverallTimeValidator performs an overall time check that
 * is delegated to (and customizable by) {@link OverallTimeAwareValidator}.
 */
class SimpleOverallTimeValidator extends OverallTimeAwareValidator<SimpleOverallTimeValidator, SimpleOverallTimeRecord> {

    public static final SimpleOverallTimeValidator DEFAULT = new SimpleOverallTimeValidator()

    @Override
    CheckablePerformanceResult validateAgainst(SimpleOverallTimeRecord previousRecord, SimpleOverallTimeRecord observedBehavior) {
        previousRecord != null ? checkOverallTime(previousRecord.overallTimeInMillis, observedBehavior.overallTimeInMillis) : CheckablePerformanceResult.SUCCESS
    }

}
