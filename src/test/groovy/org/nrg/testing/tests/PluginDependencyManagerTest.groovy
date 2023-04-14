package org.nrg.testing.tests

import org.nrg.testing.enums.PluginDependencyCheckState
import org.nrg.testing.xnat.plugins.GeneralPluginRequirement
import org.nrg.testing.xnat.plugins.PluginDependencyManager
import org.nrg.xnat.pogo.XnatPlugin
import org.testng.annotations.Test

import static org.testng.AssertJUnit.assertEquals

class PluginDependencyManagerTest {

    private static final String PLUGIN_ID = 'my_plugin'
    private static final List<XnatPlugin> defaultPlugins = [new XnatPlugin(id: PLUGIN_ID, version: '1.5-SNAPSHOT')]

    @Test
    void testPluginVersionChecksFromString() {
        assertPluginCheckSatisfied(defaultPlugins, GeneralPluginRequirement.fromString(PLUGIN_ID))
        assertPluginCheckSatisfied(defaultPlugins, GeneralPluginRequirement.fromString("${PLUGIN_ID}:1.5"))
        assertPluginCheckMismatch(defaultPlugins, GeneralPluginRequirement.fromString("${PLUGIN_ID}:1.5.1"))
        assertPluginCheckMissing(defaultPlugins, GeneralPluginRequirement.fromString('nope'))
    }

    @Test
    void testPluginVersionChecksMissing() {
        assertPluginCheckMissing(defaultPlugins, new GeneralPluginRequirement(pluginId: 'nope'))
    }

    @Test
    void testPluginVersionChecks() {
        final GeneralPluginRequirement onlyMaximum = new GeneralPluginRequirement(pluginId: PLUGIN_ID, maximumPluginVersion: '1.5')
        final GeneralPluginRequirement onlyMinimum = new GeneralPluginRequirement(pluginId: PLUGIN_ID, minimumPluginVersion: '1.1')
        final GeneralPluginRequirement boundedInterval = new GeneralPluginRequirement(pluginId: PLUGIN_ID, minimumPluginVersion: '1.2', maximumPluginVersion: '1.4')

        final Closure<Void> assertSatisfied = { String version, GeneralPluginRequirement requirement ->
            assertPluginCheckSatisfied([new XnatPlugin(id: PLUGIN_ID, version: version)], requirement)
        }
        final Closure<Void> assertMismatch = { String version, GeneralPluginRequirement requirement ->
            assertPluginCheckMismatch([new XnatPlugin(id: PLUGIN_ID, version: version)], requirement)
        }

        assertSatisfied('1.2.4-RC', onlyMaximum)
        assertSatisfied('1.4', onlyMaximum)
        assertSatisfied('1.4.9-BETA', onlyMaximum)
        assertMismatch('1.5', onlyMaximum)
        assertMismatch('1.5.1', onlyMaximum)

        assertMismatch('1.0.4-RC', onlyMinimum)
        assertSatisfied('1.1', onlyMinimum)
        assertSatisfied('1.1.2', onlyMinimum)
        assertSatisfied('2.0', onlyMinimum)

        assertMismatch('1.1.9', boundedInterval)
        assertSatisfied('1.2', boundedInterval)
        assertSatisfied('1.2.5-SNAPSHOT', boundedInterval)
        assertSatisfied('1.3.9', boundedInterval)
        assertMismatch('1.4', boundedInterval)
        assertMismatch('1.4.0', boundedInterval)
        assertMismatch('2.0', boundedInterval)
    }

    @SuppressWarnings('GrMethodMayBeStatic')
    private void assertPluginCheck(List<XnatPlugin> installedPlugins, PluginDependencyCheckState expectedState, GeneralPluginRequirement pluginRequirement) {
        assertEquals(
                expectedState,
                PluginDependencyManager.checkPlugin(installedPlugins, pluginRequirement).state
        )
    }

    private void assertPluginCheckSatisfied(List<XnatPlugin> installedPlugins,GeneralPluginRequirement pluginRequirement) {
        assertPluginCheck(installedPlugins, PluginDependencyCheckState.SATISFIED, pluginRequirement)
    }

    private void assertPluginCheckMissing(List<XnatPlugin> installedPlugins,GeneralPluginRequirement pluginRequirement) {
        assertPluginCheck(installedPlugins, PluginDependencyCheckState.MISSING_PLUGIN, pluginRequirement)
    }

    private void assertPluginCheckMismatch(List<XnatPlugin> installedPlugins,GeneralPluginRequirement pluginRequirement) {
        assertPluginCheck(installedPlugins, PluginDependencyCheckState.VERSION_MISMATCH, pluginRequirement)
    }

}
