package org.nrg.testing.xnat.performance.reset.plugin

import org.nrg.testing.xnat.conf.Settings

/**
 * Removes plugin jars from the plugins directory of an XNAT on Kubernetes through kubectl. Selected with
 * {@code xnat.performance.plugin.uninstall=kubernetes}.
 */
class KubernetesComponentUninstaller implements XnatComponentUninstaller {

    @Override
    void uninstallPlugin(String pluginName) {
        Settings.kubernetesXnat().uninstallPlugin(pluginName)
    }

    @Override
    void uninstallAllPlugins() {
        Settings.kubernetesXnat().uninstallAllPlugins()
    }

    @Override
    List<String> commandsForPlugin(String pluginName) {
        throw new UnsupportedOperationException('The Kubernetes uninstaller removes files through kubectl rather than running commands over SSH')
    }

    @Override
    List<String> commandsForAllPlugins() {
        throw new UnsupportedOperationException('The Kubernetes uninstaller removes files through kubectl rather than running commands over SSH')
    }

}
