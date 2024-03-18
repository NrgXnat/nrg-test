package org.nrg.testing.xnat.performance.persistence

class ErrorProneAggregatableRecord extends OverallTimeRecord<ErrorProneAggregatableRecord> {

    int successCount
    int failureCount

    String successRateAsPercent() {
        (100 * successCount / (successCount + failureCount)).round(1).toPlainString()
    }

}
