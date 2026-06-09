package org.nrg.testing.tests

import org.testng.annotations.AfterClass
import org.testng.annotations.Test

/**
 * Verifies the @AfterClass contract that BaseXnatTest.releaseIsolationLock relies on:
 * dependsOnMethods orders configuration methods of the same type. The method names are chosen so
 * that alphabetical fallback ordering (what TestNG would do if the dependency were ignored) is the
 * opposite of the declared dependency order, so a regression cannot pass by accident.
 */
class AfterClassOrderingTest {

    private static final List<String> ORDER = [].asSynchronized()

    @Test
    void trivial() {
    }

    @AfterClass(alwaysRun = true)
    void zRunsFirst() {
        ORDER << 'zRunsFirst'
    }

    @AfterClass(alwaysRun = true, dependsOnMethods = 'zRunsFirst')
    void aRunsSecond() {
        ORDER << 'aRunsSecond'
        assert ORDER == ['zRunsFirst', 'aRunsSecond']
    }

}
