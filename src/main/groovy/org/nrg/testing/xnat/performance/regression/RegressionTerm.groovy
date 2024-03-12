package org.nrg.testing.xnat.performance.regression

import java.util.function.Function

abstract class RegressionTerm {

    abstract Function<Double, Double> function()

    abstract String pgfPlotRepresentation()

    abstract String functionName()

}
