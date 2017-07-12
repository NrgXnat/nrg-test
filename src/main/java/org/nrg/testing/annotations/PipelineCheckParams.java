package org.nrg.testing.annotations;

import org.nrg.testing.enums.Pipeline;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PipelineCheckParams {
    Pipeline pipeline();
    int maxQueueTime() default 600; // in seconds
    int maxRuntime(); // in seconds
    String session();
    String pipelineName() default "";
    String assessorContainingPipelineHistory() default ""; // some pipelines list the pipeline execution under a previously existing assesssor (ILP under FreeSurfer assessor). Leave this blank unless you need it.
}
