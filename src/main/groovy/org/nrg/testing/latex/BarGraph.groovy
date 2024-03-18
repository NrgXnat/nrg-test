package org.nrg.testing.latex

import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

@Builder(builderStrategy = SimpleStrategy, prefix = '')
class BarGraph extends StandaloneTikzpicture<BarGraph> {

    List<Bar> bars = []
    List<String> additionalPlotOptions = []
    public static final String BASE_BAR_GRAPH = LatexUtils.loadTemplate('bargraph.tex')

    @Override
    String generatePlot() {
        final String chartWithLabelsAndOpts = LatexUtils.replaceAndMaintainIndent(
                BASE_BAR_GRAPH.replace('%CHART_LABELS%', bars*.label.join(',')),
                '%ADDITIONAL_PLOT_OPTS%',
                additionalPlotOptions.join(',\n')
        )

        LatexUtils.replaceAndMaintainIndentFromList(
                chartWithLabelsAndOpts,
                '%PLOTS%',
                bars.collect { bar ->
                    bar.producePlot()
                }
        )
    }

}
