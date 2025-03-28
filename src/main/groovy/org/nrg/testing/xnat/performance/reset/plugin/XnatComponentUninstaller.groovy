package org.nrg.testing.xnat.performance.reset.plugin

import org.nrg.testing.xnat.performance.reset.SshExecutor

trait XnatComponentUninstaller implements SshExecutor {

    abstract List<String> commandsForPlugin(String pluginName)
    
    abstract List<String> commandsForAllPlugins()

    void uninstallPlugin(String pluginName) {
        executeCommandsOverSsh(commandsForPlugin(pluginName))
    }
    
    void uninstallAllPlugins() {
        executeCommandsOverSsh(commandsForAllPlugins())
    }

}