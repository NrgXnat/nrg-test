package org.nrg.testing.xnat.performance.control

class PerformanceServerControlLookup {

    private static final String DEFAULT = 'ssh'
    private static final Map<String, PerformanceServerControl> CONTROLS = [
            (DEFAULT): new SshServerControl(),
            'kubernetes': new KubernetesServerControl()
    ]

    static PerformanceServerControl lookup(String controlKey) {
        CONTROLS.get(controlKey ?: DEFAULT)
    }

}
