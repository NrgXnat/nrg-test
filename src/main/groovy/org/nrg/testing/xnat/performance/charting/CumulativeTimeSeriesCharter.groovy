package org.nrg.testing.xnat.performance.charting

import com.google.common.graph.GraphBuilder
import com.google.common.graph.MutableGraph
import groovy.util.logging.Log4j
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression
import org.apache.commons.math3.util.Pair
import org.nrg.testing.CollectionUtils
import org.nrg.testing.latex.LatexDocument
import org.nrg.testing.latex.MultipleDatasetScatterPlot
import org.nrg.testing.latex.RegressionCurve
import org.nrg.testing.latex.ScatterPlotDataset
import org.nrg.testing.xnat.performance.actions.RepeatedMonitorableAction
import org.nrg.testing.xnat.performance.persistence.CumulativeTimeSeriesData
import org.nrg.testing.xnat.performance.regression.PolynomialRegression
import org.nrg.testing.xnat.performance.regression.ReportableRegression
import org.nrg.xnat.util.GraphUtils

import java.util.function.Function

@Log4j
class CumulativeTimeSeriesCharter extends PerformanceCharter<RepeatedMonitorableAction, CumulativeTimeSeriesData> {

    private static final Map<Integer, String> COLORS = [
            0 : 'black',
            1 : 'red',
            2 : 'blue!80!white',
            3 : 'lime!70!black'
    ]
    private static final int MAX_EQUIVALENCE_DISTANCE = 1
    private static final List<ReportableRegression> CHARTABLE_REGRESSIONS = [
            PolynomialRegression.LINEAR, PolynomialRegression.QUADRATIC, PolynomialRegression.CUBIC
    ]

    @Override
    LatexDocument produceDocument(RepeatedMonitorableAction performanceWorkflow, List<CumulativeTimeSeriesData> historicalRecord) {
        final MutableGraph<CumulativeTimeSeriesData> datasetComparisonGraph = GraphBuilder.undirected().build()
        historicalRecord.each { timeSeries ->
            datasetComparisonGraph.addNode(timeSeries)
        }
        CollectionUtils.subsetsOfSize2(historicalRecord).each { pair ->
            final double normalized = pair.v1.calculateNormalizedAverageAbsoluteValueDistance(pair.v2)
            log.info("Normalized absolute value distance from ${pair.v1.xnatVersion} -> ${pair.v2.xnatVersion}: ${normalized}")
            if (normalized < MAX_EQUIVALENCE_DISTANCE) {
                datasetComparisonGraph.putEdge(pair.v1, pair.v2)
            }
        }

        final MultipleDatasetScatterPlot scatterPlot = new MultipleDatasetScatterPlot()
                .title(performanceWorkflow.title)
                .xlabel(performanceWorkflow.actionDescription)
                .ylabel('Cumulative time in seconds')

        GraphUtils.findConnectedComponents(datasetComparisonGraph).eachWithIndex { similarDatasets, index ->
            if (similarDatasets.size() == 1) {
                scatterPlot.addDataset(datasetFromIndex(index, similarDatasets.first()))
            } else {
                final List<CumulativeTimeSeriesData> sorted = similarDatasets.sort { it.timestamp }
                final CumulativeTimeSeriesData representative = sorted.remove(0)
                scatterPlot.addDataset(datasetFromIndex(index, representative))
                scatterPlot.indicateGroupedDatasets(
                        representative.xnatVersion,
                        sorted*.xnatVersion
                )
            }
        }

        scatterPlot.datasets.sort { it.timestamp }.reverse(true)

        scatterPlot
    }

    private static ScatterPlotDataset datasetFromIndex(int index, CumulativeTimeSeriesData record) {
        final Function<Integer, String> indexToShape = { int i ->
            switch (i) {
                case 0 .. 3:
                    return '*'
                case 4 .. 7:
                    return 'triangle*'
                default:
                    return 'square*'
            }
        }

        new ScatterPlotDataset()
                .marker(indexToShape.apply(index))
                .color(COLORS[index % 4])
                .label(record.xnatVersion)
                .coordinates(record.timeSeriesData.collect { new Pair<String, String>(String.valueOf(it.key), String.valueOf(it.value / 1000.0)) })
                .timestamp(record.timestamp)
                .regressions(CHARTABLE_REGRESSIONS.collect { regression ->
                    final OLSMultipleLinearRegression performedRegression = regression.performRegression(record.timeSeriesData)
                    final List<String> regressionParams = performedRegression.estimateRegressionParameters().collect { asDouble ->
                        new BigDecimal(asDouble).toPlainString()
                    }
                    new RegressionCurve()
                            .domainMax(String.valueOf(Math.round(1.05 * record.timeSeriesData.keySet()[-1])))
                            .reference("${record.xnatVersion}-${regression.regressionName()}")
                            .displayName(regression.regressionName())
                            .function(
                                    (0 ..< regressionParams.size()).collect { termIndex ->
                                        termIndex == 0 ?
                                                String.valueOf(regressionParams[0]) :
                                                "${regressionParams[termIndex]}*${regression.regressionTerms()[termIndex - 1].pgfPlotRepresentation()}"
                                    }.join(' + ')
                            )

                })
    }

}
