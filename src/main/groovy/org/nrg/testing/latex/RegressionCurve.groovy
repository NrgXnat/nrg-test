package org.nrg.testing.latex

import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

@Builder(builderStrategy = SimpleStrategy, prefix = '')
class RegressionCurve implements LatexComponent {

    String reference
    String displayName
    String domainMax
    String function

    @Override
    String inject(String baseContent) {
        baseContent
            .replace('%REGRESSION_REF%', reference)
            .replace('%DOMAIN_MAX%', domainMax)
            .replace('%REGRESSION_FUNCTION%', function)
    }

}
