package org.nrg.testing.xnat.performance.regression

class PolynomialRegression extends ReportableRegression {

    int degree
    public static final PolynomialRegression LINEAR = new PolynomialRegression(1)
    public static final PolynomialRegression QUADRATIC = new PolynomialRegression(2)
    public static final PolynomialRegression CUBIC = new PolynomialRegression(3)

    PolynomialRegression(int degree) {
        this.degree = degree
    }

    @Override
    List<RegressionTerm> regressionTerms() {
        (1 .. degree).collect { monomialDegree ->
            new MonomialTerm(monomialDegree)
        }
    }

    @Override
    String regressionName() {
        new MonomialTerm(degree).functionName()
    }

}
