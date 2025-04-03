package org.nrg.testing.xnat.performance.reset.plugin

import org.nrg.testing.xnat.performance.reset.SshExecutor

trait XnatComponentInstaller implements SshExecutor {

    abstract List<String> commandsForPluginById(String pluginName)
    
    abstract List<String> commandsForPluginByUrl(String pluginName, String pluginUrl)
    
    abstract List<String> commandsForWarByUrl(String warUrl)

    void installPlugin(String pluginName) {
        executeCommandsOverSsh(commandsForPluginById(pluginName))
    }
    
    void installPluginWithUrl(String pluginName, String pluginUrl) {
        executeCommandsOverSsh(commandsForPluginByUrl(pluginName, pluginUrl))
    }
    
    void installWarWithUrl(String warUrl) {
        executeCommandsOverSsh(commandsForWarByUrl(warUrl))
    }

}