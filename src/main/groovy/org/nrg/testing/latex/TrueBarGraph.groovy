package org.nrg.testing.latex

class TrueBarGraph extends BarGraph {

    private static final List<String> BAR_COLORS = [
            'blue!20!white',
            'black',
            'orange!20!white',
            'white'
    ]

    @Override
    List<String> defineAdditionalOptions() {
        final List<String> baseAdditions = [
                'nodes near coords',
                'nodes near coords align = horizontal',
                "symbolic y coords = {${CHART_LABELS_PLACEHOLDER}}",
                "enlarge y limits = ${deriveEnlargedLimits(datasets[0].dataPoints.size())}" // make sure bars don't get cut off on the top and bottom
        ]
        if (datasets.size() > 1) {
            baseAdditions.addAll([
                    'xbar',
                    'area legend',
                    'legend pos = outer north east',
                    "legend entries = {${datasets*.label.join(',')}}",
                    'bar width = 0.3cm',
                    "y = ${0.2 + 0.4 * datasets.size()}cm"
            ])
        } else {
            baseAdditions << 'y = 0.8cm'
        }
        baseAdditions
    }

    @Override
    String plotDataset(int index, ChartableDataset dataset) {
        "\\addplot[xbar, fill = ${BAR_COLORS[index]}] coordinates { ${dataset.asCoordinates()} };"
    }

    double deriveEnlargedLimits(int datasetSize) {
        Math.max(0.1, (1.0 / (1 + datasetSize)).round(2))
    }

}
