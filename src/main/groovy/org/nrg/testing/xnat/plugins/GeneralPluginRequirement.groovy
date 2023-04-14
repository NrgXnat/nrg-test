package org.nrg.testing.xnat.plugins

import groovy.transform.EqualsAndHashCode
import org.nrg.testing.annotations.PluginRequirement

@EqualsAndHashCode
class GeneralPluginRequirement {

    String pluginId
    String minimumPluginVersion
    String maximumPluginVersion

    static GeneralPluginRequirement fromAnnotation(PluginRequirement pluginRequirement) {
        new GeneralPluginRequirement(
                pluginId: pluginRequirement.pluginId(),
                minimumPluginVersion: pluginRequirement.minimumSupportedVersion(),
                maximumPluginVersion: pluginRequirement.maximumSupportedVersion()
        )
    }

    static fromString(String pluginIdAndVersion) {
        final List<String> pluginStringComponents = pluginIdAndVersion.split(':')
        final String pluginId = pluginStringComponents[0]
        if (pluginStringComponents.size() > 2) {
            throw new RuntimeException("Unexpected format for plugin dependency. Expected something of the form PLUGINID or PLUGINID:OPTIONAL_VERSION, but got ${pluginIdAndVersion}")
        }
        final String requestedVersion = pluginStringComponents.size() == 2 ? pluginStringComponents[1] : null
        new GeneralPluginRequirement(
                pluginId: pluginId,
                minimumPluginVersion: requestedVersion
        )
    }

}