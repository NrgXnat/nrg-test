package org.nrg.testing.xnat.plugins

import org.nrg.testing.annotations.TestRequires
import org.nrg.testing.enums.PluginDependencyCheckState
import org.nrg.xnat.pogo.XnatPlugin

class PluginDependencyManager {

    static List<GeneralPluginRequirement> parsePluginRequirements(TestRequires requiresAnnotation) {
        final List<GeneralPluginRequirement> requirements = []
        if (requiresAnnotation) {
            requiresAnnotation.plugins().each { pluginString ->
                requirements << GeneralPluginRequirement.fromString(pluginString)
            }
            requiresAnnotation.specificPluginRequirements().each { pluginRequirement ->
                requirements << GeneralPluginRequirement.fromAnnotation(pluginRequirement)
            }
        }
        requirements
    }

    static PluginDependencyCheck checkPlugin(List<XnatPlugin> installedPlugins, GeneralPluginRequirement pluginRequirement) {
        final String pluginId = pluginRequirement.pluginId
        final XnatPlugin installedVersion = installedPlugins.find { plugin ->
            plugin.id == pluginId
        }
        if (!installedVersion) {
            return PluginDependencyCheck.MISSING_PLUGIN
        }
        final String actualVersion = installedVersion.version.split('-')[0]
        final String minReq = pluginRequirement.minimumPluginVersion
        final String maxReq = pluginRequirement.maximumPluginVersion
        if (minReq && actualVersion < minReq) {
            return new PluginDependencyCheck(
                    state: PluginDependencyCheckState.VERSION_MISMATCH,
                    failureReason: formatReason(pluginId, actualVersion, '>=', minReq)
            )
        }
        if (maxReq && actualVersion >= maxReq) {
            return new PluginDependencyCheck(
                    state: PluginDependencyCheckState.VERSION_MISMATCH,
                    failureReason: formatReason(pluginId, actualVersion, '<', maxReq)
            )
        }
        return PluginDependencyCheck.SATISFIED
    }

    private static String formatReason(String id, String installedVersion, String operator, String requiredVersion) {
        "plugin with id '${id}' was found with installed version of ${installedVersion} which does not satisfy the checked requirement to be ${operator} ${requiredVersion}"
    }

}
