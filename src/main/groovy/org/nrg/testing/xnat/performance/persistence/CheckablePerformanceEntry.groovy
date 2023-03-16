package org.nrg.testing.xnat.performance.persistence

import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property='@type')
@JsonPropertyOrder(['baseline', 'timestamp', 'xnatVersion'])
trait CheckablePerformanceEntry<X extends CheckablePerformanceEntry<X>> {

    boolean baseline = false
    Long timestamp
    String xnatVersion

}