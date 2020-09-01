package org.nrg.testing.listeners.interceptors.filters

import groovy.util.logging.Log4j
import org.nrg.testing.TestNgUtils
import org.nrg.testing.annotations.TestRequires
import org.nrg.testing.enums.TestBehavior
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
            final String missingPlugin = classRequirement.plugins().find { pluginId ->
                !pluginInstalled(pluginId)
            }
            if (missingPlugin != null) {
                log.info("XNAT plugin with id ${missingPlugin} is required for test: ${TestNgUtils.getTestName(testInstance)}. The test will be removed from consideration.")
                return false
            }
        }
        return true
    }

    @Override
    boolean isActive() {
        Settings.BEHAVIOR_FOR_MISSING_PLUGIN == TestBehavior.IGNORE
    }

    private boolean pluginInstalled(String pluginId) {
        if (installedPlugins == null) {
            final XnatInterface xnatInterface = XnatInterface.authenticate(Settings.BASEURL, Settings.DEFAULT_XNAT_CONFIG.adminUser)
            installedPlugins = xnatInterface.readInstalledPlugins()
            xnatInterface.logout()
        }
        pluginId in installedPlugins*.id
    }

}