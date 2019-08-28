package org.nrg.testing.tests.dummy

import org.nrg.testing.unit.UnitTestId
import org.nrg.testing.tests.MethodSorterTest
import org.testng.annotations.Test

@Test(groups = MethodSorterTest.DUMMY)
class TestDummyClassB {
    
    @UnitTestId(2)
    void testDummyB1() {}

    @UnitTestId(2)
    void testDummyB2() {}
    
    @UnitTestId(6)
    void testDummyNoncycleA() {}

    @UnitTestId(6)
    void testDummyNoncycleD() {}
    
    @UnitTestId(6)
    void testDummyNoncycleC() {}

    @UnitTestId(6)
    void testDummyNoncycleB() {}

}
