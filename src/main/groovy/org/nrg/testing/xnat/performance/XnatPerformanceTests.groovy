package org.nrg.testing.xnat.performance

import groovy.util.logging.Log4j
import org.nrg.testing.xnat.BaseXnatRestTest
import org.nrg.testing.xnat.performance.actions.CheckablePerformanceWorkflow
import org.testng.annotations.BeforeMethod

import java.util.function.Consumer

@Log4j
class XnatPerformanceTests extends BaseXnatRestTest {

    @BeforeMethod(alwaysRun = true)
    void clearXnat() {
        log.fatal('BeforeMethod for performance test called')
        // TODO: implement
    }

    protected PerformanceScenarioBuilder performanceScenario() {
        new PerformanceScenarioBuilder()
    }

    class PerformanceScenarioBuilder {
        private Consumer<PerformanceStateHelper> setup
        private List<CheckablePerformanceWorkflow> tests = []

        PerformanceScenarioBuilder setup(Consumer<PerformanceStateHelper> setup) {
            this.setup = setup
            this
        }

        PerformanceScenarioBuilder tests(CheckablePerformanceWorkflow... tests) {
            this.tests = tests.toList()
            this
        }

        void run() {
            new PerformanceScenario(setup, tests).run(new PerformanceStateHelper(restDriver))
        }
    }

}
