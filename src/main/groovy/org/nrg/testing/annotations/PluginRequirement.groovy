package org.nrg.testing.annotations

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Target([])
@Retention(RetentionPolicy.RUNTIME)
@interface PluginRequirement {
    String pluginId()
    String minimumSupportedVersion() default ''
    String maximumSupportedVersion() default ''
}