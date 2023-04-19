package org.nrg.testing.xnat.performance.validator

import groovy.util.logging.Log4j
import org.nrg.testing.xnat.performance.CheckablePerformanceResult
import org.nrg.testing.xnat.performance.persistence.ErrorProneAggregatableRecord

@Log4j
/**
 * ErrorProneAggregatableValidator provides validation that the record produced
 * by a {@link org.nrg.testing.xnat.performance.actions.ErrorProneAggregatableAction}
 * completes with both a sufficiently low rate of errors and also within an acceptable
 * period of time. The default allowed rate of errors is
 * {@value ErrorProneAggregatableValidator#DEFAULT_ALLOWED_RATE}. The overall time check
 * is delegated to (and customizable by) {@link OverallTimeAwareValidator}.
 */
class ErrorProneAggregatableValidator extends OverallTimeAwareValidator<ErrorProneAggregatableValidator, ErrorProneAggregatableRecord> {

    public static final double DEFAULT_ALLOWED_RATE = 0.005
    public static final ErrorProneAggregatableValidator DEFAULT = new ErrorProneAggregatableValidator()
    double allowedFailureRate = DEFAULT_ALLOWED_RATE

    ErrorProneAggregatableValidator allowedFailureRate(double rate) {
        setAllowedFailureRate(rate)
        this
    }

    @Override
    CheckablePerformanceResult validateAgainst(ErrorProneAggregatableRecord previousRecord, ErrorProneAggregatableRecord observedBehavior) {
        final CheckablePerformanceResult timeResult = previousRecord != null ? checkOverallTime(previousRecord.overallTimeInMillis, observedBehavior.overallTimeInMillis) : null
        final double observedFailureRate = (observedBehavior.failureCount.doubleValue()) / (observedBehavior.failureCount + observedBehavior.successCount)
        final CheckablePerformanceResult failureRateInducedFailure = (observedFailureRate > allowedFailureRate) ?
                new CheckablePerformanceResult("the observed failure rate of ${formatRateAsPercent(observedFailureRate)} exceeds the maximum accepted failure rate of ${formatRateAsPercent(allowedFailureRate)}.") :
                CheckablePerformanceResult.SUCCESS
        CheckablePerformanceResult.merge([timeResult, failureRateInducedFailure])
    }

}
