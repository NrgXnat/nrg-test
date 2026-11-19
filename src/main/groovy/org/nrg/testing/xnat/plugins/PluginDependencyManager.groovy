package org.nrg.testing.xnat.plugins

import org.nrg.testing.annotations.TestRequires
import org.nrg.xnat.pogo.XnatPlugin
import org.nrg.xnat.versions.plugins.GeneralPluginRequirement
import org.nrg.xnat.versions.plugins.PluginDependencyCheck
import org.nrg.xnat.versions.plugins.PluginDependencyCheckState

import java.util.regex.Matcher

class PluginDependencyManager {

    // The dot-separated numbers a version starts with, as long as they end at a suffix such as '-xpl', '.dirty'
    // or '+build' and not in the middle of a part such as '3rc1'.
    private static final String LEADING_NUMBERS = /^[0-9]+(\.[0-9]+)*(?![0-9A-Za-z]|\.[0-9])/

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
     * Compares two versions by the dot-separated numbers they start with, part by part as numbers, so that
     * 4.10.0 is later than 4.9.0. Comparing them as text puts 4.10.0 before 4.9.0, which drops every test
     * that requires 4.9.0 on a server that runs 4.10.0. A suffix such as '-rc1' or '.dirty' is ignored and a
     * missing part counts as 0, so 4.9 equals 4.9.0. A version that doesn't start with numbers can't be
     * compared that way, so those fall back to the text comparison.
     */
    private static int compareVersions(String left, String right) {
        final List<BigInteger> leftParts = leadingNumbers(left)
        final List<BigInteger> rightParts = leadingNumbers(right)
        if (leftParts == null || rightParts == null) {
            return left <=> right
        }
        for (int i = 0; i < Math.max(leftParts.size(), rightParts.size()); i++) {
            final int comparison = (i < leftParts.size() ? leftParts[i] : BigInteger.ZERO) <=> (i < rightParts.size() ? rightParts[i] : BigInteger.ZERO)
            if (comparison != 0) {
                return comparison
            }
        }
        0
    }

    // BigInteger, because a part can be a build timestamp that doesn't fit in an int.
    private static List<BigInteger> leadingNumbers(String version) {
        final Matcher matcher = version =~ LEADING_NUMBERS
        matcher.find() ? matcher.group().split(/\./).collect { new BigInteger(it) } : null
    }

    private static String formatReason(String id, String installedVersion, String operator, String requiredVersion) {
        "plugin with id '${id}' was found with installed version of ${installedVersion} which does not satisfy the checked requirement to be ${operator} ${requiredVersion}"
    }

}
