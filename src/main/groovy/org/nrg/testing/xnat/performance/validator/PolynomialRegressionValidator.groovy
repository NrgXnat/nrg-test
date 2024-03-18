package org.nrg.testing.xnat.performance.validator

import groovy.util.logging.Log4j
import org.nrg.testing.xnat.performance.regression.PolynomialRegression
import org.nrg.testing.xnat.performance.regression.ReportableRegression

@Log4j
/**
 * PolynomialRegressionValidator implements simple polynomial regression for the more generic
 * {@link HistoricalRegressionValidator}.
 */
class PolynomialRegressionValidator extends HistoricalRegressionValidator<PolynomialRegressionValidator> {

    public static final PolynomialRegressionValidator LINEAR = new PolynomialRegressionValidator(1) // i.e. affine
    public static final PolynomialRegressionValidator QUADRATIC = new PolynomialRegressionValidator(2)
    public static final PolynomialRegressionValidator STRICT_QUADRATIC = new PolynomialRegressionValidator(2).acceptableRSquared(0.999)
    public static final PolynomialRegressionValidator CUBIC = new PolynomialRegressionValidator(3)

    int degree

    PolynomialRegressionValidator(int degree) {
        setDegree(degree)
    }

    @Override
    ReportableRegression defineRegression() {
        new PolynomialRegression(degree)
    }

}
