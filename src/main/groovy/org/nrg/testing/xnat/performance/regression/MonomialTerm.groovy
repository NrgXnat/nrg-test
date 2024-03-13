package org.nrg.testing.xnat.performance.regression

import java.util.function.Function

class MonomialTerm extends RegressionTerm {

    int degree

    MonomialTerm(int degree) {
        this.degree = degree
    }

    @Override
    Function<Double, Double> function() {
        { double x -> Math.pow(x, degree) }
    }

    @Override
    String pgfPlotRepresentation() {
        "(\\x)^${degree}"
    }

    @Override
    String functionName() {
        switch (degree) {
            case 1:
                return 'Lin'
            case 2:
                return 'Quad'
            case 3:
                return 'Cub'
            default:
                return "degree-${degree}"
        }
    }

}
