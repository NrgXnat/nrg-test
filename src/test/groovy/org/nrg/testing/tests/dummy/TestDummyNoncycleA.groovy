package org.nrg.testing.tests.dummy

import org.nrg.testing.unit.UnitTestId
import org.nrg.testing.tests.MethodSorterTest
import org.testng.annotations.Test

@Test(groups = MethodSorterTest.DUMMY)
class TestDummyNoncycleA {

    @UnitTestId(8)
    void testDummyNoncycleClassA() {}

}
