package org.nrg.testing.xnat.performance.charting

import org.apache.commons.math3.util.Pair
import org.nrg.testing.latex.LatexDocument
import org.nrg.testing.latex.MultipleDatasetScatterPlot
import org.nrg.testing.latex.ScatterPlotDataset
import org.nrg.testing.xnat.performance.actions.RepeatedMonitorableAction
import org.nrg.testing.xnat.performance.persistence.CumulativeTimeSeriesData

class CumulativeTimeSeriesCharter extends PerformanceCharter<RepeatedMonitorableAction, CumulativeTimeSeriesData> {

    private static final Map<Integer, String> COLORS = [
            0 : 'black',
            1 : 'red',
            2 : 'blue!80!white',
            3 : 'lime!70!black'
    ]

    @Override
    LatexDocument produceDocument(RepeatedMonitorableAction performanceWorkflow, List<CumulativeTimeSeriesData> historicalRecord) {
        final MultipleDatasetScatterPlot scatterPlot = new MultipleDatasetScatterPlot()
                .title(performanceWorkflow.title)
                .xlabel(performanceWorkflow.actionDescription)
                .ylabel('Cumulative time in seconds')
        historicalRecord.eachWithIndex { record, index ->
            scatterPlot.addDataset(
                    datasetFromIndex(index)
                            .label(record.xnatVersion)
                            .coordinates(record.timeSeriesData.collect { new Pair<String, String>(String.valueOf(it.key), String.valueOf(it.value / 1000.0)) })
            )
        }
        scatterPlot
    }

    private static ScatterPlotDataset datasetFromIndex(int index) {
        new ScatterPlotDataset()
                .marker(index > 3 ? 'triangle*' : '*')
                .color(COLORS[index % 4])
    }

}
