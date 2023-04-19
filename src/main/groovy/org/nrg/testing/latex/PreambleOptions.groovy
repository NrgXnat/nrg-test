package org.nrg.testing.latex

import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

@Builder(builderStrategy = SimpleStrategy, prefix = '')
class PreambleOptions implements LatexComponent {

    List<String> additionalLines

    @Override
    String inject(String baseContent) {
        baseContent.replace('%PREAMBLE_OPTIONS%', (additionalLines ?: []).join('\n'))
    }

}
