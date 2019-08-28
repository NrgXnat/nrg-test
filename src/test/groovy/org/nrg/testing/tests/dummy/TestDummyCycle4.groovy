package org.nrg.testing.tests.dummy

import org.nrg.testing.unit.UnitTestId
import org.nrg.testing.annotations.SoftClassDependency
import org.nrg.testing.tests.MethodSorterTest
import org.testng.annotations.Test

@SoftClassDependency([TestDummyCycle3])
class TestDummyCycle4 {

    @Test(groups = MethodSorterTest.DUMMY)
    @UnitTestId(9)
    void testDummyCycleClass4() {}

}
