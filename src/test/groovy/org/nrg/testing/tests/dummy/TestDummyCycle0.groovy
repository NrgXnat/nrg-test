package org.nrg.testing.tests.dummy

import org.nrg.testing.unit.UnitTestId
import org.nrg.testing.annotations.SoftClassDependency
import org.nrg.testing.tests.MethodSorterTest
import org.testng.annotations.Test

@SoftClassDependency([TestDummyCycle1, TestDummyCycle4])
class TestDummyCycle0 {

    @Test(groups = MethodSorterTest.DUMMY)
    @UnitTestId(9)
    void testDummyCycleClass0() {}

}
