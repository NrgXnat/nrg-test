package org.nrg.testing.latex

import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

@Builder(builderStrategy = SimpleStrategy, prefix = '')
class DataPoint {

    String x
    String y
    String pointMeta

    String asCoordinate() {
        "(${x},${y})" + ((pointMeta != null) ? "[${pointMeta}]" : '')
    }

    String asTableEntry() {
        "    ${x} ${y}" + ((pointMeta != null) ? " ${pointMeta}" : '')
    }

}
