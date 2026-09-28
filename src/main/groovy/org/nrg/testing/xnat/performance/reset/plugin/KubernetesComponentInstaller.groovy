package org.nrg.testing.xnat.performance.reset.plugin

import org.nrg.testing.xnat.conf.Settings

/**
 * Installs plugins into the plugins directory of an XNAT on Kubernetes by copying the jar in through kubectl; they
 * load at the next restart, which the Kubernetes reset performs. Selected with
 * {@code xnat.performance.plugin.install=kubernetes}.
 */
class KubernetesComponentInstaller implements XnatComponentInstaller {

    @Override
    void installPlugin(String pluginName) {
        Settings.kubernetesXnat().installPlugin(pluginName)
    }

    @Override
    void installPluginWithUrl(String pluginName, String pluginUrl) {
        Settings.kubernetesXnat().installPluginFromUrl(pluginName, pluginUrl)
    }

    @Override
    void installWarWithUrl(String warUrl) {
        throw new UnsupportedOperationException('On Kubernetes an XNAT version is an image, not a WAR; the kubernetes server control stages it')
    }

    @Override
    List<String> commandsForPluginById(String pluginName) {
        throw new UnsupportedOperationException('The Kubernetes installer copies files through kubectl rather than running commands over SSH')
    }

    @Override
    List<String> commandsForPluginByUrl(String pluginName, String pluginUrl) {
        throw new UnsupportedOperationException('The Kubernetes installer copies files through kubectl rather than running commands over SSH')
    }

    @Override
    List<String> commandsForWarByUrl(String warUrl) {
        throw new UnsupportedOperationException('On Kubernetes an XNAT version is an image, not a WAR')
    }

}
