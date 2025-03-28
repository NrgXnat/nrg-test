package org.nrg.testing.annotations

import org.nrg.testing.xnat.performance.XnatPerformanceTests
import org.testng.annotations.Test

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * A shortcut to define performance tests by setting the appropriate dataProvider and group.
 * If your performance test needs to specify more properties of the underlying {@link org.testng.annotations.Test}
 * annotation, it is not necessary to use this annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.METHOD, ElementType.TYPE])
@Test(groups = 'performance', dataProvider = XnatPerformanceTests.DEPLOYMENTS_PROVIDER)
@interface PerformanceTest {

}