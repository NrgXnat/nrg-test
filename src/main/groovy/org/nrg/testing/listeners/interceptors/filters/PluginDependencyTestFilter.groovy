package org.nrg.testing.listeners.interceptors.filters

import groovy.util.logging.Log4j
import org.nrg.testing.TestNgUtils
import org.nrg.testing.annotations.TestRequires
import org.nrg.testing.enums.PluginDependencyCheckState
import org.nrg.testing.enums.TestBehavior
import org.nrg.testing.xnat.plugins.GeneralPluginRequirement
import org.nrg.testing.xnat.plugins.PluginDependencyCheck
import org.nrg.testing.xnat.plugins.PluginDependencyManager
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

        final Map<GeneralPluginRequirement, PluginDependencyCheck> allPluginRequirements = [:]
        [classRequirement, methodRequirement].each { possibleRequirementSource ->
            PluginDependencyManager.parsePluginRequirements(possibleRequirementSource).each { pluginRequirement ->
                allPluginRequirements.put(pluginRequirement, PluginDependencyManager.checkPlugin(cachePlugins(), pluginRequirement))
            }
        }
        final GeneralPluginRequirement missingPlugin = allPluginRequirements.find {
            it.value.state == PluginDependencyCheckState.MISSING_PLUGIN
        }?.key
        if (missingPlugin) {
            log.info("XNAT plugin with id ${missingPlugin.pluginId} is required for test: ${TestNgUtils.getTestName(testInstance)}. The test will be removed from consideration.")
            return false
        }
        final Map.Entry<GeneralPluginRequirement, PluginDependencyCheck> versionMismatchPlugin = allPluginRequirements.find {
            it.value.state == PluginDependencyCheckState.VERSION_MISMATCH
        }
        if (versionMismatchPlugin) {
            log.info("XNAT plugin with id ${versionMismatchPlugin.key.pluginId} is required for test: ${TestNgUtils.getTestName(testInstance)}. The plugin appears to be installed, but with an incompatible version: ${versionMismatchPlugin.value.failureReason}. The test will be removed from consideration.")
            return false
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