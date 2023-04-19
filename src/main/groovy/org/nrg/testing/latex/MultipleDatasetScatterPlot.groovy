package org.nrg.testing.latex

class MultipleDatasetScatterPlot extends StandaloneTikzpicture<MultipleDatasetScatterPlot> {

    List<ScatterPlotDataset> datasets = []
    public static final String BASE_SCATTERPLOT = LatexUtils.loadTemplate('multi_scatterplot.tex')
    public static final String BASE_SCATTERPLOT_INDIVIDUAL_DATASET = LatexUtils.loadTemplate('multi_scatterplot_individual_dataset.tex')

    MultipleDatasetScatterPlot addDataset(ScatterPlotDataset dataset) {
        datasets << dataset
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
        LatexUtils.replaceAndMaintainIndentFromList(
                BASE_SCATTERPLOT.replace('%LEGEND_ENTRIES%', datasets.collect { "\\switchocg{${it.label}}{${it.label}}" }.join(',')),
                '%PLOTS%',
                datasets.collect { dataset ->
                    dataset.inject(BASE_SCATTERPLOT_INDIVIDUAL_DATASET)
                }
        )
    }

}
