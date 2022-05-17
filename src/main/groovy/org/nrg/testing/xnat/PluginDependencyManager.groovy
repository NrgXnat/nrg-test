package org.nrg.testing.xnat

import org.nrg.testing.enums.PluginDependencyCheckState
import org.nrg.testing.util.VersionParser
import org.nrg.xnat.pogo.XnatPlugin

class PluginDependencyManager {

    static PluginDependencyCheckState checkPlugin(List<XnatPlugin> installedPlugins, String pluginIdAndVersion) {
        final List<String> pluginStringComponents = pluginIdAndVersion.split(':')
        final String pluginId = pluginStringComponents[0]
        if (pluginStringComponents.size() > 2) {
            throw new RuntimeException("Unexpected format for plugin dependency. Expected something of the form PLUGINID or PLUGINID:OPTIONAL_VERSION, but got ${pluginIdAndVersion}")
        }
        final String requestedVersion = pluginStringComponents.size() == 2 ? pluginStringComponents[1] : null
        if (!(pluginId in installedPlugins*.id)) {
            return PluginDependencyCheckState.MISSING_PLUGIN
        }
        if (requestedVersion != null) {
            final String foundVersion = installedPlugins.find { plugin ->
                plugin.id == pluginId
            }.version
            if (!VersionParser.versionCompatible(requestedVersion, foundVersion)) {
                return PluginDependencyCheckState.VERSION_MISMATCH
            }
        }
        return PluginDependencyCheckState.SATISFIED
    }

}
