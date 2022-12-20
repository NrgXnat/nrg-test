package org.nrg.testing.xnat.performance.reset

class XnatResetScriptLookup {

    private static final Map<String, PerformanceServerResetScript> RESET_SCRIPTS = [
            'internal_local': new InternalXnatReset(false),
            'internal_ssh': new InternalXnatReset(true),
            'nothing': new DoNothingXnatReset()
    ]

    static PerformanceServerResetScript lookup(String managerKey) {
        RESET_SCRIPTS.get(managerKey)
    }

}
