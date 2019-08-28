package org.nrg.testing.tests.dummy

import org.nrg.testing.unit.UnitTestId
import org.nrg.testing.annotations.SoftClassDependency
import org.nrg.testing.tests.MethodSorterTest
import org.testng.annotations.Test

@SoftClassDependency(TestDummyCycle0)
@Test(groups = MethodSorterTest.DUMMY)
class TestDummyCycle2 {

    @UnitTestId(9)
    void testDummyCycleClass2() {}

}
