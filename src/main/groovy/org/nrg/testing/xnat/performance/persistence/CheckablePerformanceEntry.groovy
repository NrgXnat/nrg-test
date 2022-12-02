package org.nrg.testing.xnat.performance.persistence

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property='@type')
trait CheckablePerformanceEntry<X extends CheckablePerformanceEntry<X>> {

    @JsonInclude(JsonInclude.Include.NON_NULL) Boolean baseline
    Long timestamp
    String xnatVersion

}