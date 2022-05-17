package org.nrg.testing.listeners.interceptors.filters

import groovy.util.logging.Log4j
import org.nrg.testing.TestNgUtils
import org.nrg.testing.annotations.TestRequires
import org.nrg.testing.enums.PluginDependencyCheckState
import org.nrg.testing.enums.TestBehavior
import org.nrg.testing.xnat.PluginDependencyManager
import org.nrg.testing.xnat.conf.Settings
import org.nrg.xnat.interfaces.XnatInterface
import org.nrg.xnat.pogo.XnatPlugin
import org.testng.IMethodInstance

@Log4j
class PluginDependencyTestFilter extends TestFilterInterceptor {

    private List<XnatPlugin> installedPlugins = null

    PluginDependencyTestFilter() {
        super()
    }

    @Override
    boolean isTestAllowed(IMethodInstance testInstance) {
        final TestRequires classRequirement = testInstance.method.realClass.getAnnotation(TestRequires) as TestRequires
        final TestRequires methodRequirement = TestNgUtils.getAnnotation(testInstance.method, TestRequires)
        final List<String> requiredPlugins = []

        if (classRequirement != null) {
            requiredPlugins.addAll(classRequirement.plugins())
        }
        if (methodRequirement != null) {
            requiredPlugins.addAll(methodRequirement.plugins())
        }

        if (!requiredPlugins.isEmpty()) {
            for (String plugin : requiredPlugins) {
                switch (PluginDependencyManager.checkPlugin(cachePlugins(), plugin)) {
                    case PluginDependencyCheckState.MISSING_PLUGIN:
                        log.info("XNAT plugin with id (and possibly minimum version) ${plugin} is required for test: ${TestNgUtils.getTestName(testInstance)}. The test will be removed from consideration.")
                        return false
                    case PluginDependencyCheckState.VERSION_MISMATCH:
                        log.info("XNAT plugin with id and minimum version ${plugin} is required for test: ${TestNgUtils.getTestName(testInstance)}. The plugin appears to be installed, but with an incompatible version. The test will be removed from consideration.")
                        return false
                }
            }
        }
        return true
    }

    @Override
    boolean isActive() {
        Settings.BEHAVIOR_FOR_MISSING_PLUGIN == TestBehavior.IGNORE
    }

    private List<XnatPlugin> cachePlugins() {
        if (installedPlugins == null) {
            final XnatInterface xnatInterface = XnatInterface.authenticate(Settings.BASEURL, Settings.DEFAULT_XNAT_CONFIG.adminUser)
            installedPlugins = xnatInterface.readInstalledPlugins()
            xnatInterface.logout()
        }
        installedPlugins
    }

}