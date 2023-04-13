package org.nrg.testing.xnat.performance.validator

import groovy.util.logging.Log4j
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression
import org.nrg.testing.xnat.performance.CheckablePerformanceResult
import org.nrg.testing.xnat.performance.persistence.CumulativeTimeSeriesData

import java.util.function.Function

@Log4j
/**
 * HistoricalRegressionValidator provides validation that the time series data produced
 * by a {@link org.nrg.testing.xnat.performance.actions.RepeatedMonitorableAction}
 * matches a functional form and also completes within an acceptable
 * period of time. The abstract method {@link #regressionTerms()} is used to define
 * the terms to be added together to create the regression model. The default minimum coefficient
 * of determination (R-squared) is {@value HistoricalRegressionValidator#DEFAULT_ACCEPTABLE_RSQUARED},
 * but can be overwritten with {@link #acceptableRSquared(double)}. The overall time check
 * is delegated to (and customizable by) {@link OverallTimeAwareValidator}.
 */
abstract class HistoricalRegressionValidator<X extends HistoricalRegressionValidator<X>> extends
        OverallTimeAwareValidator<X, CumulativeTimeSeriesData> {

    public static final double DEFAULT_ACCEPTABLE_RSQUARED = 0.99
    double acceptableRSquared = DEFAULT_ACCEPTABLE_RSQUARED

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
