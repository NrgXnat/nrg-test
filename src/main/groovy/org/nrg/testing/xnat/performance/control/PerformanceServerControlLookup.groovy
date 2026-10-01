package org.nrg.testing.xnat.performance.control

import org.nrg.testing.xnat.conf.XNATProperties

class PerformanceServerControlLookup {

    private static final String DEFAULT = 'ssh'
    private static final Map<String, PerformanceServerControl> CONTROLS = [
            (DEFAULT): new SshServerControl(),
            'kubernetes': new KubernetesServerControl()
    ]

    static PerformanceServerControl lookup(String controlKey) {
        final String key = controlKey ?: DEFAULT
        final PerformanceServerControl control = CONTROLS.get(key)
        if (control == null) {
            throw new IllegalArgumentException("Unknown performance server control ${key}: set ${XNATProperties.PERFORMANCE_SERVER_CONTROL}, " +
                    "or ${XNATProperties.PERFORMANCE_PLATFORM} when that is unset, to one of ${CONTROLS.keySet()}")
        }
        control
    }

}
