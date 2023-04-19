package org.nrg.testing.latex

import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

@Builder(builderStrategy = SimpleStrategy, prefix = '')
class LatexPackage implements LatexComponent {

    List<String> options
    String packageName

    @Override
    String inject(String baseContent) {
        baseContent
                .replace('%PACKAGE_OPTIONS%', LatexUtils.joinOptionalList(options))
                .replace('%PACKAGE_NAME%', packageName)
    }

}
