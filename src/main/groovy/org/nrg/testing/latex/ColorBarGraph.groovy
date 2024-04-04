package org.nrg.testing.latex

/**
 * Implements a "bar chart" that is really a scatter plot in disguise as a bar chart. Custom drawing in of bars comes
 * from the very helpful answer here https://tex.stackexchange.com/a/537356 .
 */
class ColorBarGraph extends BarGraph {

    @Override
    String getPointMeta() {
        '\\thisrow{successrate}'
    }

    @Override
    List<String> defineAdditionalOptions() {
        [
                "yticklabels = {${CHART_LABELS_PLACEHOLDER}}",
                'colorbar',
                'colorbar style = {xshift = 1cm, title = Success rate}',
                'colormap={successcolormap}{rgb255(0)=(226, 220, 245) rgb255(1000)=(76, 62, 118)}',
                'scatter',
                'scatter src = x',
                'only marks',
                'visualization depends on={\\thisrow{x} \\as \\barlength}',
                'scatter/@pre marker code/.append code={',
                '    \\fill [draw=black] (axis direction cs:0,0.25) rectangle (axis direction cs:{-1 * \\barlength},-0.25);',
                '    \\pgfplotsset{mark = none}', // the bar we manually construct in the previous line is our marker
                '}'
        ]
    }

    @Override
    String plotDataset(int index, ChartableDataset dataset) {
        final List<String> lines = [
                '\\addplot table[x = x,y expr=\\coordindex] {',
                '    x    y    successrate'
        ]
        lines << dataset.asTableEntries()
        lines << '};'
        lines.join('\n')
    }

}
