package org.nrg.testing.xnat.performance.validator

import groovy.util.logging.Log4j
import org.nrg.testing.xnat.performance.CheckablePerformanceResult
import org.nrg.testing.xnat.performance.persistence.ErrorProneAggregatableRecord

@Log4j
class ErrorProneAggregatableValidator implements
        PerformanceValidator<ErrorProneAggregatableRecord>,
        OverallTimeAwareValidator<ErrorProneAggregatableValidator> {

    double allowedFailureRate = 0.005
    public static final ErrorProneAggregatableValidator DEFAULT = new ErrorProneAggregatableValidator()

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
