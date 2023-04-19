package org.nrg.testing.latex

import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

@Builder(builderStrategy = SimpleStrategy, prefix = '')
class DocumentClass implements LatexComponent {

    List<String> options
    String className

    @Override
    String inject(String baseContent) {
        baseContent
                .replace('%DOC_OPTIONS%', LatexUtils.joinOptionalList(options))
                .replace('%DOC_CLASS%', className)
    }

}
