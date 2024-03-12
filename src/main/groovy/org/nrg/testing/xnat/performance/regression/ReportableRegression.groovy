package org.nrg.testing.xnat.performance.regression

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression

abstract class ReportableRegression {

    abstract List<RegressionTerm> regressionTerms()

    abstract String regressionName()

    OLSMultipleLinearRegression performRegression(Map<Integer, Long> observations) {
        final List<RegressionTerm> regressionTerms = regressionTerms()
        final OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression()
        final double[] y = new double[observations.size()]
        final double[][] x = new double[observations.size()][regressionTerms.size()]

        observations.eachWithIndex{ Map.Entry<Integer, Long> entry, int index ->
            y[index] = entry.value / 1000.0
            regressionTerms.eachWithIndex { RegressionTerm regressionTerm, int regressionIndex ->
                x[index][regressionIndex] = regressionTerm.function().apply(entry.key)
            }
        }
        regression.newSampleData(y, x)
        regression
    }

}
