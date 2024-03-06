package org.nrg.testing.latex

import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import org.apache.commons.math3.util.Pair

@Builder(builderStrategy = SimpleStrategy, prefix = '')
class ScatterPlotDataset implements LatexComponent {

    String label
    String color = 'black'
    String marker = '*'
    String markerSize = '1.5 pt'
    Long timestamp
    List<Pair<String, String>> coordinates

    @Override
    String inject(String baseContent) {
        baseContent
                .replace('%PLOT_COLOR%', color)
                .replace('%PLOT_MARKER%', marker)
                .replace('%PLOT_SIZE%', markerSize)
                .replace('%PLOT_LABEL%', label)
                .replace('%PLOT_COORDINATES%', coordinates.collect { coordinate ->
                    "(${coordinate.key},${coordinate.value})"
                }.join(''))
    }

}
