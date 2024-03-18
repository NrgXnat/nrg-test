package org.nrg.testing.latex

import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

@Builder(builderStrategy = SimpleStrategy, prefix = '')
class BarWithColorDimension extends Bar {

    String colorMapValue
    String explicitPointMeta
    public static final String BASE_PLOT = LatexUtils.loadTemplate('bargraph_bar_colormapped.tex')

    @Override
    String inject(String baseContent) {
        super.inject(baseContent)
                .replace('%COLOR_MAP_VALUE%', colorMapValue)
                .replace('%POINT_META%', explicitPointMeta)
    }

    @Override
    String producePlot() {
        inject(BASE_PLOT)
    }

}
