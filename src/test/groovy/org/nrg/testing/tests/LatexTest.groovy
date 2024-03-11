package org.nrg.testing.tests

import org.apache.commons.math3.util.Pair
import org.nrg.testing.FileIOUtils
import org.nrg.testing.latex.MultipleDatasetScatterPlot
import org.nrg.testing.latex.ScatterPlotDataset
import org.testng.AssertJUnit
import org.testng.annotations.Test

class LatexTest {

    @Test
    void testMultipleDatasetScatterPlotGeneration() {
        AssertJUnit.assertEquals(
                FileIOUtils.loadResource('multi_scatterplot_test.tex').text,
                new MultipleDatasetScatterPlot()
                        .xlabel("X-label")
                        .ylabel("Y-label")
                        .title('Graph title')
                        .addDataset(
                                new ScatterPlotDataset()
                                        .label('A')
                                        .color('blue')
                                        .coordinates([new Pair<>('1', '10'), new Pair<>('2', '20')])
                        ).addDataset(
                                new ScatterPlotDataset()
                                        .label('B')
                                        .color('red')
                                        .coordinates([new Pair<>('1', '15'), new Pair<>('2', '25')])
                        ).indicateGroupedDatasets('A', ['C', 'D'])
                        .produceSourceDocument()
        )
    }

}
