package org.nrg.testing.xnat.performance.reset.plugin

class InternalPluginUninstaller implements XnatPluginUninstaller {

    @Override
    List<String> commands(String pluginName) {
        [
                "rm -f /home/xnat/plugins/${pluginName}"
        ]
    }

}
