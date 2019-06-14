package org.nrg.testing.annotations;

import com.jayway.restassured.internal.http.Method;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TestedApiSpecs {
    TestedApiSpecs.Spec[] value();

    @Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Spec {
        Method[] method();
        String[] url();
    }
}
