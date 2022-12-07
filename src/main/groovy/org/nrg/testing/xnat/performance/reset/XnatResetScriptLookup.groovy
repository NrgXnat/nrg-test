package org.nrg.testing.xnat.performance.reset

class XnatResetScriptLookup {

    private static final Map<String, PerformanceServerResetScript> RESET_SCRIPTS = [
            'internal': new InternalXnatReset()
    ]

    static PerformanceServerResetScript lookup(String managerKey) {
        RESET_SCRIPTS.get(managerKey)
    }

}
