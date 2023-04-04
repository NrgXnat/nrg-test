package org.nrg.testing.xnat.performance.validator

import org.nrg.testing.xnat.performance.CheckablePerformanceResult

/**
 * OverallTimeAwareValidator defines reusable functionality to check
 * that the overall time to complete an action is within an acceptable fraction
 * of a previous expected/baseline time. The default tolerance is set to
 * 0.05, but can be overwritten with {@link OverallTimeAwareValidator#endpointTolerance(double)}
 */
trait OverallTimeAwareValidator<X extends PerformanceValidator> {

    public static final double DEFAULT_TOLERANCE = 0.05
    double endpointTolerance = DEFAULT_TOLERANCE

    X endpointTolerance(double tol) {
        setEndpointTolerance(tol)
        this as X
    }

    CheckablePerformanceResult checkOverallTime(long baseline, long observed) {
        final double performancePenaltyScore = (observed - baseline).doubleValue() / baseline
        (performancePenaltyScore > endpointTolerance) ?
            new CheckablePerformanceResult("the overall execution time of ${observed} ms exceeded the baseline execution time of ${baseline} ms by ${format(performancePenaltyScore)}, which is larger than the acceptable maximum of ${format(endpointTolerance)}") :
            CheckablePerformanceResult.SUCCESS
    }

    String format(double rate) {
        (this as X).formatRateAsPercent(rate)
    }

}