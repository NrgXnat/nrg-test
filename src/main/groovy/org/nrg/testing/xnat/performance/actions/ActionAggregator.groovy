package org.nrg.testing.xnat.performance.actions

import org.apache.commons.lang3.time.StopWatch
import org.nrg.testing.TimeUtils

class ActionAggregator {

    int successCount = 0
    int failureCount = 0
    StopWatch stopWatch
    private Long totalTime

    ActionAggregator() {
        stopWatch = TimeUtils.launchStopWatch()
    }

    void incrementSuccess(int numSuccesses = 1) {
        successCount += numSuccesses
    }

    void incrementFailure(int numFailures = 1) {
        failureCount += numFailures
    }

    void endMonitorablePeriod() {
        totalTime = stopWatch.time
    }

    long getTotalTime() {
        totalTime ?: stopWatch.time
    }

}
