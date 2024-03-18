package org.nrg.testing.xnat.performance.persistence

abstract class OverallTimeRecord<T extends OverallTimeRecord<T>> implements CheckablePerformanceEntry<T> {

    long overallTimeInMillis

}
