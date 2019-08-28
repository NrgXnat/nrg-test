package org.nrg.testing.tests.dummy

import org.nrg.testing.unit.UnitTestId
import org.nrg.testing.annotations.SoftClassDependency
import org.nrg.testing.tests.MethodSorterTest
import org.testng.annotations.Test

@SoftClassDependency(TestDummyCycle3)
@Test(groups = MethodSorterTest.DUMMY)
class TestDummyCycle4 {

    @UnitTestId(9)
    void testDummyCycleClass4() {}

}
