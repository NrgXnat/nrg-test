package org.nrg.testing.tests

import org.nrg.testing.UnitTestUtils
import org.nrg.testing.xnat.performance.persistence.CumulativeTimeSeriesData
import org.nrg.testing.xnat.performance.validator.PolynomialRegressionValidator
import org.testng.annotations.Test

class MathTest {

    private static final Map<Integer, Long> TRUE_LINEAR = [
            0 : 10,
            1 : 8,
            3 : 4
    ]
    private static final Map<Integer, Long> TRUE_QUADRATIC = [
            0 : -1,
            1 : 0,
            2 : 3,
            3 : 8
    ]
    private static final Map<Integer, Long> SYMMETRIC_QUADRATIC = [
            (-2) : 4,
            (-1) : 1,
            0 : 0,
            1 : 1,
            2 : 4
    ]

    @Test
    void testLinearRegression() {
        UnitTestUtils.assertDoubleEqual(
                1,
                PolynomialRegressionValidator.LINEAR.defineRegression().performRegression(TRUE_LINEAR).calculateRSquared()
        )
        UnitTestUtils.assertDoubleEqual(
                0,
                PolynomialRegressionValidator.LINEAR.defineRegression().performRegression(SYMMETRIC_QUADRATIC).calculateRSquared()
        )
    }

    @Test
    void testQuadraticRegression() {
        UnitTestUtils.assertDoubleEqual(
                1,
                PolynomialRegressionValidator.QUADRATIC.defineRegression().performRegression(TRUE_QUADRATIC).calculateRSquared()
        )
    }

    @Test
    void testTimeSeriesDistance() {
        UnitTestUtils.assertDoubleEqual(
                2500/207,
                new CumulativeTimeSeriesData(timeSeriesData: [1: 10000, 2: 20000]).calculateNormalizedAverageAbsoluteValueDistance(
                        new CumulativeTimeSeriesData(timeSeriesData: [1: 13000, 2: 16000])
                )
        )
    }

}
