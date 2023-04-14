package org.nrg.testing.xnat.plugins

import org.nrg.testing.enums.PluginDependencyCheckState

class PluginDependencyCheck {

    PluginDependencyCheckState state
    String failureReason
    public static final PluginDependencyCheck SATISFIED = new PluginDependencyCheck(state: PluginDependencyCheckState.SATISFIED)
    public static final PluginDependencyCheck MISSING_PLUGIN = new PluginDependencyCheck(state: PluginDependencyCheckState.MISSING_PLUGIN)

}
