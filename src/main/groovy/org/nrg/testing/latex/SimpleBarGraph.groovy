package org.nrg.testing.latex

class SimpleBarGraph extends StandaloneTikzpicture<SimpleBarGraph> {

    List<Bar> bars = []
    public static final String BASE_BAR_GRAPH = LatexUtils.loadTemplate('bargraph.tex')
    public static final String BASE_PLOT = LatexUtils.loadTemplate('bargraph_bar.tex')

    SimpleBarGraph addBar(String label, String value) {
        bars << new Bar(label: label, value: value)
        this
    }

    @Override
    String generatePlot() {
        LatexUtils.replaceAndMaintainIndentFromList(
                BASE_BAR_GRAPH.replace('%CHART_LABELS%', bars*.label.join(',')),
                '%PLOTS%',
                bars.collect { bar ->
                    bar.inject(BASE_PLOT)
                }
        )
    }

    private static class Bar implements LatexComponent {
        String label
        String value

        @Override
        String inject(String baseContent) {
            baseContent
                    .replace('%LABEL%', label)
                    .replace('%VALUE%', value)
        }
    }

}
