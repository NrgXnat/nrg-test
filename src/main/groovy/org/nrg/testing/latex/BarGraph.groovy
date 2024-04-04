package org.nrg.testing.latex

import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

@Builder(builderStrategy = SimpleStrategy, prefix = '')
abstract class BarGraph extends StandaloneTikzpicture<BarGraph> {

    List<ChartableDataset> datasets = []
    public static final String BASE_BAR_GRAPH = LatexUtils.loadTemplate('bargraph.tex')
    public static final CHART_LABELS_PLACEHOLDER = '%CHART_LABELS%'

    String getPointMeta() {
        'x'
    }

    ChartableDataset datasetFor(String datasetName) {
        final ChartableDataset existing = datasets.find { it.label == datasetName }
        if (existing != null) {
            existing
        } else {
            final ChartableDataset newDataset = new ChartableDataset().label(datasetName)
            datasets << newDataset
            newDataset
        }
    }

    abstract List<String> defineAdditionalOptions()

    abstract String plotDataset(int index, ChartableDataset dataset)

    @Override
    String generatePlot() {
        final String chartWithOpts = LatexUtils.replaceAndMaintainIndent(
                BASE_BAR_GRAPH,
                '%ADDITIONAL_PLOT_OPTS%',
                (defineAdditionalOptions() + ["point meta=${getPointMeta()}"]).join(',\n')
        )

        int datasetIndex = -1
        final String mostlyResolvedChart = LatexUtils.replaceAndMaintainIndentFromList(
                chartWithOpts,
                '%PLOTS%',
                datasets.collect { dataset ->
                    datasetIndex++
                    plotDataset(datasetIndex, dataset)
                }
        )

        mostlyResolvedChart.replace(CHART_LABELS_PLACEHOLDER, datasets[0].dataPoints*.y.join(','))
    }

}
