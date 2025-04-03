package org.nrg.testing.xnat.performance.reset.plugin

class InternalComponentUninstaller implements XnatComponentUninstaller {

    @Override
    List<String> commandsForPlugin(String pluginName) {
        [
                "rm -f /home/xnat/plugins/${pluginName}"
        ]
    }

    @Override
    List<String> commandsForAllPlugins() {
        [
                'rm -f /home/xnat/plugins/*'
        ]
    }
    
}
