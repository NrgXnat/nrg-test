package org.nrg.testing.xnat.performance.reset.plugin

import org.nrg.testing.xnat.performance.reset.SshExecutor

trait XnatPluginInstaller implements SshExecutor {

    abstract List<String> commands(String pluginName)

    void installPlugin(String pluginName) {
        executeCommandsOverSsh(commands(pluginName))
    }

}