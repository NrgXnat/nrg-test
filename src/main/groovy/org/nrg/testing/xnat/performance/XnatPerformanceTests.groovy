package org.nrg.testing.xnat.performance

import groovy.util.logging.Log4j
import org.nrg.testing.xnat.BaseXnatRestTest
import org.nrg.testing.xnat.conf.Settings
import org.nrg.testing.xnat.performance.actions.CheckablePerformanceWorkflow
import org.nrg.testing.xnat.ssh.SSHConnection
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

import java.util.function.Consumer

@Log4j
@Test(groups = 'performance')
class XnatPerformanceTests extends BaseXnatRestTest {

    @BeforeMethod(alwaysRun = true)
    protected void clearXnat() {
        log.fatal('BeforeMethod for performance test called')
        if (Settings.PERFORMANCE_TESTS_ALLOWED && !Settings.PERFORMANCE_EXPORT_ONLY) {
            log.info("Performing hard reset on XNAT server...")
            Settings.PERFORMANCE_RESET_SCRIPT.resetXnatServer()
            SSHConnection.waitForTomcat()
            setupXnat()
        }
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
            if (!Settings.PERFORMANCE_EXPORT_ONLY) {
                new PerformanceScenario(setup, tests).run(new PerformanceStateHelper(restDriver))
            }

            tests.each { test ->
                test.getPerformanceCharter().chart(test)
            }
        }
    }

}
