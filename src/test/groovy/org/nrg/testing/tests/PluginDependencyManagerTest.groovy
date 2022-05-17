package org.nrg.testing.tests

import org.nrg.testing.enums.PluginDependencyCheckState
import org.nrg.testing.xnat.PluginDependencyManager
import org.nrg.xnat.pogo.XnatPlugin
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

import static org.testng.AssertJUnit.assertEquals

class PluginDependencyManagerTest {

    private static final String PLUGIN_ID = 'my_plugin'
    private final List<XnatPlugin> plugins = []

    @BeforeClass
    void formPlugin() {
        final XnatPlugin myPlugin = new XnatPlugin().id(PLUGIN_ID)
        myPlugin.setVersion('1.5-SNAPSHOT')
        plugins << myPlugin
    }

    @Test
    void testPluginSatisfiedWithoutVersion() {
        assertEquals(
                PluginDependencyCheckState.SATISFIED,
                PluginDependencyManager.checkPlugin(plugins, PLUGIN_ID)
        )
    }

    @Test
    void testPluginMissingWithoutVersion() {
        assertEquals(
                PluginDependencyCheckState.MISSING_PLUGIN,
                PluginDependencyManager.checkPlugin(plugins, 'missing')
        )
    }

    @Test
    void testPluginSatisfiedWithVersion() {
        assertEquals(
                PluginDependencyCheckState.SATISFIED,
                PluginDependencyManager.checkPlugin(plugins, 'my_plugin:1.5')
        )
    }

    @Test
    void testPluginMissingWithVersion() {
        assertEquals(
                PluginDependencyCheckState.MISSING_PLUGIN,
                PluginDependencyManager.checkPlugin(plugins, 'missing:1.5')
        )
    }

    @Test
    void testPluginVersionMismatch() {
        assertEquals(
                PluginDependencyCheckState.VERSION_MISMATCH,
                PluginDependencyManager.checkPlugin(plugins, 'my_plugin:1.6')
        )
    }

}
