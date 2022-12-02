package org.nrg.testing.xnat.performance.validator

import groovy.util.logging.Log4j
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression
import org.nrg.testing.xnat.performance.CheckablePerformanceResult
import org.nrg.testing.xnat.performance.persistence.CumulativeTimeSeriesData

import java.util.function.Function

@Log4j
abstract class HistoricalRegressionValidator<X extends HistoricalRegressionValidator<X>> implements
        PerformanceValidator<CumulativeTimeSeriesData>,
        OverallTimeAwareValidator<X> {

    double acceptableRSquared = 0.95

    abstract List<Function<Double, Double>> regressionTerms()

    X acceptableRSquared(double acceptable) {
        setAcceptableRSquared(acceptable)
        this as X
    }

    @Override
    CheckablePerformanceResult validateAgainst(CumulativeTimeSeriesData previousRecord, CumulativeTimeSeriesData observedBehavior) {
        final OLSMultipleLinearRegression regression = performRegression(observedBehavior.timeSeriesData)
        final double rSquared = regression.calculateRSquared()
        if (rSquared < acceptableRSquared) {
            return new CheckablePerformanceResult("the expected shape of the time series data didn't seem to match observed reality: coefficient of determination (R-squared) value of ${rSquared} of the regression is lower than minimum acceptable of ${acceptableRSquared}.")
        }
        if (previousRecord != null) {
            return checkOverallTime(
                    previousRecord.timeSeriesData.values().max(),
                    observedBehavior.timeSeriesData.values().max()
            )
        }
        CheckablePerformanceResult.SUCCESS
    }

    OLSMultipleLinearRegression performRegression(Map<Integer, Long> observations) {
        final List<Function<Double, Double>> regressionTerms = regressionTerms()
        final OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression()
        final double[] y = new double[observations.size()]
        final double[][] x = new double[observations.size()][regressionTerms.size()]

        observations.eachWithIndex{ Map.Entry<Integer, Long> entry, int index ->
            y[index] = entry.value
            regressionTerms.eachWithIndex { Function<Double, Double> regressionTerm, int regressionIndex ->
                x[index][regressionIndex] = regressionTerm.apply(entry.key)
            }
        }
        regression.newSampleData(y, x)
        regression
    }

}
