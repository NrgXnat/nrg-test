package org.nrg.testing.xnat.plugins

import org.nrg.testing.annotations.TestRequires
import org.nrg.xnat.pogo.XnatPlugin
import org.nrg.xnat.versions.plugins.GeneralPluginRequirement
import org.nrg.xnat.versions.plugins.PluginDependencyCheck

class PluginDependencyManager {

    static List<GeneralPluginRequirement> parsePluginRequirements(TestRequires requiresAnnotation) {
        final List<GeneralPluginRequirement> requirements = []
        if (requiresAnnotation) {
            requiresAnnotation.plugins().each { pluginString ->
                requirements << GeneralPluginRequirement.fromString(pluginString)
            }
            requiresAnnotation.specificPluginRequirements().each { pluginRequirement ->
                requirements << new GeneralPluginRequirement(
                        pluginId: pluginRequirement.pluginId(),
                        minimumPluginVersion: pluginRequirement.minimumSupportedVersion(),
                        maximumPluginVersion: pluginRequirement.maximumSupportedVersion()
                )
            }
        }
        requirements
    }

    // GeneralPluginRequirement compares the versions as numbers, so that 4.10.0 is later than 4.9.0.
    static PluginDependencyCheck checkPlugin(List<XnatPlugin> installedPlugins, GeneralPluginRequirement pluginRequirement) {
        pluginRequirement.checkAgainst(installedPlugins)
    }

}
