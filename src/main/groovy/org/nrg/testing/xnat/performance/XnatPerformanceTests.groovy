package org.nrg.testing.xnat.performance

import groovy.util.logging.Log4j
import org.nrg.testing.annotations.PerformanceTestPlugin
import org.nrg.testing.xnat.BaseXnatRestTest
import org.nrg.testing.xnat.conf.Settings
import org.nrg.testing.xnat.conf.XnatConfig
import org.nrg.testing.xnat.performance.actions.CheckablePerformanceWorkflow
import org.nrg.testing.xnat.ssh.SSHConnection
import org.nrg.xnat.pogo.CustomXnatDeployment
import org.nrg.xnat.pogo.PluginRegistry
import org.nrg.xnat.pogo.XnatDeployment
import org.nrg.xnat.pogo.plugins.BitbucketDownloadsDerivation
import org.testng.annotations.AfterSuite
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

import java.lang.reflect.Method
import java.util.function.Consumer

import static org.testng.AssertJUnit.fail

@Log4j
@Test(groups = 'performance', dataProvider = DEPLOYMENTS_PROVIDER)
class XnatPerformanceTests extends BaseXnatRestTest {

    protected static final List<CheckablePerformanceWorkflow> executedTests = []
    protected static final Set<String> knownTempPlugins = []
    public static final String DEPLOYMENTS_PROVIDER = 'deployments'

    @DataProvider
    static Object[][] deployments() {
        Settings.PERFORMANCE_DEPLOYMENTS.collect { deployment ->
            [deployment]
        }.toArray(Object[][]::new)
    }

    @BeforeMethod(alwaysRun = true)
    protected void clearXnat(Method method, Object[] methodParams) {
        if (performanceTestsRunning()) {
            final PerformanceTestPlugin performanceTestPlugin = method.getAnnotation(PerformanceTestPlugin)
            final Set<String> requestedPlugins = ((performanceTestPlugin != null) ? performanceTestPlugin.value() : []) as Set<String>
            knownTempPlugins.addAll(requestedPlugins)
            final XnatDeployment xnatDeployment = methodParams[0] as XnatDeployment
            if (!(xnatDeployment instanceof CustomXnatDeployment)) {
                final SSHConnection sshConnection = new SSHConnection()
                sshConnection.stopTomcat()

                log.info('Attempting to remove all plugins from XNAT...')
                Settings.PERFORMANCE_PLUGIN_UNINSTALLER.uninstallAllPlugins()
                
                xnatDeployment.plugins.each { plugin ->
                    Settings.PERFORMANCE_PLUGIN_INSTALLER.installPluginWithUrl(plugin.id, plugin.downloadUrl)
                }
                
                final BitbucketDownloadsDerivation xnatDownloads = new BitbucketDownloadsDerivation(PluginRegistry.XNAT_DEV, 'xnat-web')
                final String downloadUrl = xnatDownloads.apply(xnatDeployment.xnatVersionString == '1.7.6' ? '1.7.6-tc8' : xnatDeployment.xnatVersionString)
                Settings.PERFORMANCE_PLUGIN_INSTALLER.installWarWithUrl(downloadUrl)
                constructRestDriver(XnatConfig.buildDefaultConfig().xnatVersion(xnatDeployment.xnatVersion))
            }

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

    protected PerformanceScenarioBuilder performanceScenario(XnatDeployment xnatDeployment) {
        new PerformanceScenarioBuilder(xnatDeployment)
    }

    protected boolean performanceTestsRunning() {
        Settings.PERFORMANCE_TESTS_ALLOWED && !Settings.PERFORMANCE_EXPORT_ONLY
    }

    class PerformanceScenarioBuilder {
        private String deploymentRepresentation
        private Consumer<PerformanceStateHelper> setup
        private List<CheckablePerformanceWorkflow> tests = []

        PerformanceScenarioBuilder(XnatDeployment xnatDeployment) {
            deploymentRepresentation = xnatDeployment instanceof CustomXnatDeployment ? Settings.XNAT_VERSION_AS_STRING : xnatDeployment.id
        }

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
                    new PerformanceScenario(setup, tests).run(new PerformanceStateHelper(deploymentRepresentation, restDriver)) : null

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
                            record.xnatVersion == deploymentRepresentation
                        }
                    })
        }
    }

}
