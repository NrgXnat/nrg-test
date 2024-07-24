package org.nrg.testing.xnat.performance

import groovy.util.logging.Log4j
import org.nrg.testing.annotations.PerformanceTestPlugin
import org.nrg.testing.xnat.BaseXnatRestTest
import org.nrg.testing.xnat.Users
import org.nrg.testing.xnat.conf.Settings
import org.nrg.testing.xnat.performance.actions.CheckablePerformanceWorkflow
import org.nrg.testing.xnat.ssh.SSHConnection
import org.nrg.xnat.pogo.users.User
import org.testng.annotations.AfterSuite
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

import java.lang.reflect.Method
import java.util.function.Consumer

import static org.testng.AssertJUnit.fail

@Log4j
@Test(groups = 'performance')
class XnatPerformanceTests extends BaseXnatRestTest {

    protected static final List<CheckablePerformanceWorkflow> executedTests = []
    protected static final Set<String> knownTempPlugins = []

    @BeforeMethod(alwaysRun = true)
    protected void clearXnat(Method method) {
        if (performanceTestsRunning()) {
            final PerformanceTestPlugin performanceTestPlugin = method.getAnnotation(PerformanceTestPlugin)
            final Set<String> requestedPlugins = ((performanceTestPlugin != null) ? performanceTestPlugin.value() : []) as Set<String>
            knownTempPlugins.addAll(requestedPlugins)

            knownTempPlugins.each { plugin ->
                if (plugin in requestedPlugins) {
                    log.info("Attempting to install plugin '${plugin}' temporarily for a test...")
                    Settings.PERFORMANCE_PLUGIN_INSTALLER.installPlugin(plugin)
                } else {
                    uninstallPlugin(plugin)
                }
            }

            hardResetXnat()
            SSHConnection.waitForTomcat()
            setupXnat()
            createRequiredUsers()
        }
    }

    @AfterSuite(alwaysRun = true)
    protected void uninstallPlugins() {
        if (performanceTestsRunning()) {
            knownTempPlugins.each { plugin ->
                uninstallPlugin(plugin)
            }
            hardResetXnat()
        }
    }

    @AfterSuite(alwaysRun = true)
    protected void produceComparativeCharts() {
        executedTests.each { test ->
            test.getComparativeCharters().each { charter ->
                log.info("Producing comparative charts for ${test.identifier}...")
                charter.chart(test)
            }
        }
    }

    protected void uninstallPlugin(String plugin) {
        log.info("Attempting to remove plugin '${plugin}' from XNAT (which may have already been done)...")
        Settings.PERFORMANCE_PLUGIN_UNINSTALLER.uninstallPlugin(plugin)
    }

    protected void hardResetXnat() {
        log.info("Performing hard reset on XNAT server...")
        Settings.PERFORMANCE_RESET_SCRIPT.resetXnatServer()
    }

    protected PerformanceScenarioBuilder performanceScenario() {
        new PerformanceScenarioBuilder()
    }


    @Override
    protected void createRequiredUsers() {
        final User metricsUser = Users.constructMainAccount('metrics', 'admin')
        mainAdminInterface().assignUserToRoles(metricsUser, Settings.METRICS_API_USER_ROLE);
    }

    protected boolean performanceTestsRunning() {
        Settings.PERFORMANCE_TESTS_ALLOWED && !Settings.PERFORMANCE_EXPORT_ONLY
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
            final String failureResult = attemptToRun() ?
                    new PerformanceScenario(setup, tests).run(new PerformanceStateHelper(restDriver)) : null

            tests.each { test ->
                test.getEffectiveCharter().chart(test)
                executedTests << test
            }

            if (failureResult) {
                fail(failureResult)
            }
        }

        private boolean attemptToRun() {
            !Settings.PERFORMANCE_EXPORT_ONLY &&
                    (!Settings.PERFORMANCE_NEW_TESTS_ONLY || tests.any { test ->
                        !PerformanceUtils.readHistory(test.identifier).entries.any { record ->
                            record.xnatVersion == Settings.XNAT_VERSION_AS_STRING
                        }
                    })
        }
    }

}
