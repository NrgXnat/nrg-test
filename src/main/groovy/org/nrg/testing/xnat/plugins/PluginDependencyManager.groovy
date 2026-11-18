package org.nrg.testing.xnat.plugins

import org.nrg.testing.annotations.TestRequires
import org.nrg.xnat.pogo.XnatPlugin
import org.nrg.xnat.versions.Version
import org.nrg.xnat.versions.plugins.GeneralPluginRequirement
import org.nrg.xnat.versions.plugins.PluginDependencyCheck
import org.nrg.xnat.versions.plugins.PluginDependencyCheckState

class PluginDependencyManager {

    private static final String NUMERIC_VERSION = /[0-9]+(\.[0-9]+)*/

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
        if (minReq && compareVersions(actualVersion, minReq) < 0) {
            return new PluginDependencyCheck(
                    state: PluginDependencyCheckState.VERSION_MISMATCH,
                    failureReason: formatReason(pluginId, actualVersion, '>=', minReq)
            )
        }
        if (maxReq && compareVersions(actualVersion, maxReq) >= 0) {
            return new PluginDependencyCheck(
                    state: PluginDependencyCheckState.VERSION_MISMATCH,
                    failureReason: formatReason(pluginId, actualVersion, '<', maxReq)
            )
        }
        return PluginDependencyCheck.SATISFIED
    }

    /**
     * Compares two version strings part by part as numbers, so that 4.10.0 is later than 4.9.0. Comparing
     * them as text puts 4.10.0 before 4.9.0, which drops every test that requires 4.9.0 on a server that
     * runs 4.10.0. A version that isn't dot-separated numbers can't be compared that way, so those fall
     * back to the text comparison.
     */
    private static int compareVersions(String left, String right) {
        if (left ==~ NUMERIC_VERSION && right ==~ NUMERIC_VERSION) {
            return new Version(left) <=> new Version(right)
        }
        left <=> right
    }

    private static String formatReason(String id, String installedVersion, String operator, String requiredVersion) {
        "plugin with id '${id}' was found with installed version of ${installedVersion} which does not satisfy the checked requirement to be ${operator} ${requiredVersion}"
    }

}
