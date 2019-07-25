package org.nrg.testing.annotations

import org.nrg.testing.enums.TestData

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Allows specifying flags which will be picked up to check requirements for tests or perform an action (create user, download data).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.METHOD, ElementType.TYPE])
@interface TestRequires {
    boolean ssh() default false
    boolean db() default false
    boolean dicomScp() default false
    boolean openXnat() default false
    boolean closedXnat() default false
    int users() default 0
    boolean admin() default false
    String[] plugins() default []
    TestData[] data() default [TestData.NONE]
}
