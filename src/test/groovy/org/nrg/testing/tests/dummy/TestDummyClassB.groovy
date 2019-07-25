package org.nrg.testing.tests.dummy

import org.nrg.testing.UnitTestId
import org.nrg.testing.tests.MethodSorterTest
import org.testng.annotations.Test

class TestDummyClassB {

    @Test(groups = MethodSorterTest.DUMMY)
    @UnitTestId(2)
    void testDummyB1() {}

    @Test(groups = MethodSorterTest.DUMMY)
    @UnitTestId(2)
    void testDummyB2() {}

    @Test(groups = MethodSorterTest.DUMMY)
    @UnitTestId(6)
    void testDummyNoncycleA() {}

    @Test(groups = MethodSorterTest.DUMMY)
    @UnitTestId(6)
    void testDummyNoncycleD() {}

    @Test(groups = MethodSorterTest.DUMMY)
    @UnitTestId(6)
    void testDummyNoncycleC() {}

    @Test(groups = MethodSorterTest.DUMMY)
    @UnitTestId(6)
    void testDummyNoncycleB() {}

}
