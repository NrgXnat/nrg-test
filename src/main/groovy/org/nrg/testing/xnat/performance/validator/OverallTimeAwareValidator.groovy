package org.nrg.testing.xnat.performance.validator

import org.nrg.testing.xnat.performance.CheckablePerformanceResult
import org.nrg.testing.xnat.performance.persistence.CheckablePerformanceEntry

/**
 * OverallTimeAwareValidator defines reusable functionality to check
 * that the overall time to complete an action is within an acceptable fraction
 * of a previous expected/baseline time. The default tolerance is set to
 * {@value #DEFAULT_TOLERANCE}, but can be overwritten
 * with {@link OverallTimeAwareValidator#endpointTolerance(double)}
 */
abstract class OverallTimeAwareValidator<X extends OverallTimeAwareValidator<X, Y>, Y extends CheckablePerformanceEntry<Y>>
        extends PerformanceValidator<Y> {

    public static final double DEFAULT_TOLERANCE = 0.05
    double endpointTolerance = DEFAULT_TOLERANCE

    X endpointTolerance(double tol) {
        setEndpointTolerance(tol)
        this as X
    }

    CheckablePerformanceResult checkOverallTime(long baseline, long observed) {
        final double performancePenaltyScore = (observed - baseline).doubleValue() / baseline
        (performancePenaltyScore > endpointTolerance) ?
            new CheckablePerformanceResult("the overall execution time of ${observed} ms exceeded the baseline execution time of ${baseline} ms by ${formatRateAsPercent(performancePenaltyScore)}, which is larger than the acceptable maximum of ${formatRateAsPercent(endpointTolerance)}") :
            CheckablePerformanceResult.SUCCESS
    }

}