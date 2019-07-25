package org.nrg.testing.annotations

import org.nrg.testing.enums.Pipeline

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface PipelineLaunchParams {
    int estimatedRuntime() // in minutes
    String session()
    Pipeline pipeline()
    String pipelineLaunchName() default ''
    String pipelineHistoryName() default ''
    boolean customLaunch() default false // Used for pipelines where they are not launched in the build menu. If set to true, you must launch the pipeline and get back to the session all through the config
}
