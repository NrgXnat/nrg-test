package org.nrg.testing.xnat.performance.reset.plugin

import org.nrg.testing.xnat.performance.reset.SshExecutor

trait XnatPluginUninstaller implements SshExecutor {

    abstract List<String> commands(String pluginName)

    void uninstallPlugin(String pluginName) {
        executeCommandsOverSsh(commands(pluginName))
    }

}