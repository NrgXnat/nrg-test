package org.nrg.testing.latex

import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

@Builder(builderStrategy = SimpleStrategy, prefix = '')
class Bar implements LatexComponent {

    String label
    String value
    public static final String BASE_PLOT = LatexUtils.loadTemplate('bargraph_bar.tex')

    @Override
    String inject(String baseContent) {
        baseContent
                .replace('%LABEL%', label)
                .replace('%VALUE%', value)
    }

    String producePlot() {
        inject(BASE_PLOT)
    }

}
