package org.nrg.testing.xnat.performance.validator

import org.nrg.testing.xnat.performance.CheckablePerformanceResult

trait OverallTimeAwareValidator<X extends PerformanceValidator> {

    double endpointTolerance = 0.05

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