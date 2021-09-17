package org.nrg.testing.annotations

import io.restassured.http.Method

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Target([ElementType.METHOD])
@Retention(RetentionPolicy.RUNTIME)
@interface TestedApiSpec {
    Method[] method()
    String[] url()
}