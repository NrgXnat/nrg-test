package org.nrg.testing.xnat.performance

import groovy.util.logging.Log4j
import org.nrg.testing.xnat.performance.actions.CheckablePerformanceWorkflow

import java.util.function.Consumer

import static org.testng.AssertJUnit.fail

@Log4j
class PerformanceScenario {

    Consumer<PerformanceStateHelper> setup
    List<CheckablePerformanceWorkflow> tests

    PerformanceScenario(Consumer<PerformanceStateHelper> setup, List<CheckablePerformanceWorkflow> tests) {
        this.setup = setup
        this.tests = tests
    }

    String run(PerformanceStateHelper performanceStateHelper) {
        if (setup != null) {
            setup.accept(performanceStateHelper)
        }
        final Map<CheckablePerformanceWorkflow, CheckablePerformanceResult> failingResults = (tests.collectEntries { test ->
            [(test): test.run(performanceStateHelper)]
        } as Map<CheckablePerformanceWorkflow, CheckablePerformanceResult>).findAll { resultEntry ->
            !resultEntry.value.passed
        }
        if (failingResults.size() > 0) {
            final String errorMessage = (['PerformanceScenario did not meet acceptable criteria. The following checks failed:'] +
                    failingResults.collect { failingEntry ->
                        "  * ${failingEntry.key.identifier} failed because ${failingEntry.value.problems.join(', ')}".toString()
                    }).join('\n')
            log.info(errorMessage)
            return errorMessage
        }
        null
    }

}
