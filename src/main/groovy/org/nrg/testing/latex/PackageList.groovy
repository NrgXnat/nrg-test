package org.nrg.testing.latex

import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

@Builder(builderStrategy = SimpleStrategy, prefix = '')
class PackageList implements LatexComponent {

    List<LatexPackage> packages
    public static final String PACKAGE_TEMPLATE = LatexUtils.loadTemplate('packages.tex')

    @Override
    String inject(String baseContent) {
        baseContent.replace('%PACKAGES%', packages.collect { it.inject(PACKAGE_TEMPLATE) }.join('\n'))
    }

}
