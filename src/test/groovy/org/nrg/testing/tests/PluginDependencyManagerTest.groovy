package org.nrg.testing.tests

import org.nrg.testing.xnat.plugins.PluginDependencyManager
import org.nrg.xnat.pogo.XnatPlugin
import org.nrg.xnat.versions.plugins.GeneralPluginRequirement
import org.nrg.xnat.versions.plugins.PluginDependencyCheckState
import org.testng.annotations.Test

import static org.testng.AssertJUnit.assertEquals

class PluginDependencyManagerTest {

    private static final String PLUGIN_ID = 'examplePlugin'

    @Test
    void aTwoDigitMinorVersionSatisfiesAOneDigitMinimum() {
        // As text, "4.10.3" sorts before "4.9.0".
        assertEquals(PluginDependencyCheckState.SATISFIED, check('4.10.3', '4.9.0', null))
        assertEquals(PluginDependencyCheckState.SATISFIED, check('4.10.3-xpl', '4.6.0', null))
        assertEquals(PluginDependencyCheckState.SATISFIED, check('1.10', '1.9.2', null))
        assertEquals(PluginDependencyCheckState.SATISFIED, check('10.0.0', '9.5.1', null))
    }

    @Test
    void anOlderVersionDoesNotSatisfyTheMinimum() {
        assertEquals(PluginDependencyCheckState.VERSION_MISMATCH, check('4.9.0', '4.10.0', null))
        assertEquals(PluginDependencyCheckState.VERSION_MISMATCH, check('4.5.2', '4.6.0', null))
    }

    @Test
    void theMinimumIsInclusiveAndTheMaximumIsExclusive() {
        assertEquals(PluginDependencyCheckState.SATISFIED, check('4.9.0', '4.9.0', null))
        assertEquals(PluginDependencyCheckState.SATISFIED, check('4.9.0', '4.9', null))
        assertEquals(PluginDependencyCheckState.SATISFIED, check('4.9.5', null, '4.10.0'))
        assertEquals(PluginDependencyCheckState.VERSION_MISMATCH, check('4.10.0', null, '4.10.0'))
        assertEquals(PluginDependencyCheckState.VERSION_MISMATCH, check('4.11.0', null, '4.9.0'))
    }

    @Test
    void aVersionThatIsNotNumericIsStillComparedAsText() {
        assertEquals(PluginDependencyCheckState.SATISFIED, check('b2', 'b1', null))
        assertEquals(PluginDependencyCheckState.VERSION_MISMATCH, check('b1', 'b2', null))
    }

    @Test
    void aPluginThatIsNotInstalledIsMissing() {
        final GeneralPluginRequirement requirement = new GeneralPluginRequirement(pluginId: 'anotherPlugin', minimumPluginVersion: '1.0')
        assertEquals(PluginDependencyCheckState.MISSING_PLUGIN, PluginDependencyManager.checkPlugin([new XnatPlugin(id: PLUGIN_ID, version: '1.0')], requirement).state)
    }

    private static PluginDependencyCheckState check(String installed, String minimum, String maximum) {
        final GeneralPluginRequirement requirement = new GeneralPluginRequirement(pluginId: PLUGIN_ID, minimumPluginVersion: minimum, maximumPluginVersion: maximum)
        PluginDependencyManager.checkPlugin([new XnatPlugin(id: PLUGIN_ID, version: installed)], requirement).state
    }

}
