package org.nrg.testing.xnat.performance.persistence

class ErrorProneAggregatableRecord implements CheckablePerformanceEntry<ErrorProneAggregatableRecord> {

    int successCount
    int failureCount
    long overallTimeInMillis

}
