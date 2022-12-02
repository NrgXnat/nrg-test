package org.nrg.testing.xnat.performance.actions

import org.apache.commons.lang3.time.StopWatch
import org.nrg.testing.TimeUtils

class ActionMonitor {

    StopWatch stopWatch
    private Long totalTime

    ActionMonitor() {
        stopWatch = TimeUtils.launchStopWatch()
    }

    void reset() {
        stopWatch.reset()
        stopWatch.start()
    }

    void endMonitorablePeriod() {
        totalTime = stopWatch.time
    }

    long getTotalTime() {
        totalTime ?: stopWatch.time
    }

}
