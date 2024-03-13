package org.nrg.testing.xnat.performance.regression

class ExponentialRegression extends ReportableRegression {

    private static final RegressionTerm TERM = new ExponentialTerm()

    @Override
    List<RegressionTerm> regressionTerms() {
        [TERM]
    }

    @Override
    String regressionName() {
        TERM.functionName()
    }

}
