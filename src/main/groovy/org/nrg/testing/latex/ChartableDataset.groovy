package org.nrg.testing.latex

import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

@Builder(builderStrategy = SimpleStrategy, prefix = '')
class ChartableDataset {

    String label
    List<DataPoint> dataPoints = []

    String asCoordinates() {
        dataPoints*.asCoordinate().join(' ')
    }

    String asTableEntries() {
        dataPoints*.asTableEntry().join('\n')
    }

}
