package org.nrg.testing.tests.dummy

import org.nrg.testing.unit.UnitTestId
import org.nrg.testing.annotations.SoftClassDependency
import org.nrg.testing.tests.MethodSorterTest
import org.testng.annotations.Test

@SoftClassDependency(TestDummyCycle2)
@Test(groups = MethodSorterTest.DUMMY)
class TestDummyCycle1 {

    @UnitTestId(9)
    void testDummyCycleClass1() {}

}
