package org.nrg.testing.xnat.performance.regression

import java.util.function.Function

class ExponentialTerm extends RegressionTerm {

    @Override
    Function<Double, Double> function() {
        Math::exp
    }

    @Override
    String pgfPlotRepresentation() {
        "e^(\\x)"
    }

    @Override
    String functionName() {
        'Exp'
    }

}
