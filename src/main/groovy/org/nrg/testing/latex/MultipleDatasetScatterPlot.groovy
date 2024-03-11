package org.nrg.testing.latex

class MultipleDatasetScatterPlot extends StandaloneTikzpicture<MultipleDatasetScatterPlot> {

    List<ScatterPlotDataset> datasets = []
    Map<String, List<String>> groupedDatasets = [:]
    public static final String BASE_SCATTERPLOT = LatexUtils.loadTemplate('multi_scatterplot.tex')
    public static final String BASE_SCATTERPLOT_INDIVIDUAL_DATASET = LatexUtils.loadTemplate('multi_scatterplot_individual_dataset.tex')
    public static final String BASE_DATASET_COMBINATION_EXPLANATION = LatexUtils.loadTemplate('combined_datasets.tex')

    MultipleDatasetScatterPlot addDataset(ScatterPlotDataset dataset) {
        datasets << dataset
        this
    }

    MultipleDatasetScatterPlot indicateGroupedDatasets(String representativeDataset, List<String> hiddenDatasets) {
        groupedDatasets.put(representativeDataset, hiddenDatasets)
        this
    }

    @Override
    PackageList specifyPackages() {
        final PackageList packageList = super.specifyPackages()
        packageList.packages << new LatexPackage().options(['tikz']).packageName('ocgx2')
        packageList
    }

    @Override
    String generatePlot() {
        final String plot = LatexUtils.replaceAndMaintainIndentFromList(
                BASE_SCATTERPLOT.replace('%LEGEND_ENTRIES%', datasets.collect { "\\switchocg{${it.label}}{${it.label}}" }.join(',')),
                '%PLOTS%',
                datasets.collect { dataset ->
                    dataset.inject(BASE_SCATTERPLOT_INDIVIDUAL_DATASET)
                }
        )

        LatexUtils.replaceAndMaintainIndent(plot, '%CHART_EXPLANATION%', generateExplanation())
    }

    String generateExplanation() {
        if (groupedDatasets.isEmpty()) {
            ''
        } else {
            int verticalOffset = 3.5
            LatexUtils.replaceAndMaintainIndentFromList(
                    BASE_DATASET_COMBINATION_EXPLANATION,
                    '%DATASET_ITEMS%',
                    groupedDatasets.collect { grouping ->
                        verticalOffset += 0.5
                        "\\node at (-1,-${verticalOffset}) {\$\\bullet\$ ${grouping.key} also closely represents ${grouping.value.join(', ')}};"
                    }
            )
        }
    }

}
