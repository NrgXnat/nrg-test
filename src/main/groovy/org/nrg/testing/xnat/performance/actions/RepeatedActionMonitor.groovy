package org.nrg.testing.xnat.performance.actions

import org.apache.commons.lang3.time.StopWatch
import org.nrg.testing.TimeUtils

class RepeatedActionMonitor {

    int actionsPerSplit
    int currentActions = 0
    StopWatch timer
    Map<Integer, Long> splitsInMillis

    RepeatedActionMonitor(int actionsPerSplit) {
        this.actionsPerSplit = actionsPerSplit
    }

    void start() {
        splitsInMillis = [:]
        timer = TimeUtils.launchStopWatch()
    }

    void split() {
        currentActions += actionsPerSplit
        splitsInMillis.put(currentActions, timer.time)
    }

}
