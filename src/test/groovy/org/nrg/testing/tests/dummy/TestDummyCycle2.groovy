package org.nrg.testing.tests.dummy

import org.nrg.testing.UnitTestId
import org.nrg.testing.annotations.SoftClassDependency
import org.nrg.testing.tests.MethodSorterTest
import org.testng.annotations.Test

@SoftClassDependency(TestDummyCycle0)
class TestDummyCycle2 {

    @Test(groups = MethodSorterTest.DUMMY)
    @UnitTestId(9)
    void testDummyCycleClass2() {}

}
