package org.nrg.testing.tests.dummy

import org.nrg.testing.unit.UnitTestId
import org.nrg.testing.annotations.SoftClassDependency
import org.nrg.testing.tests.MethodSorterTest
import org.testng.annotations.Test

@SoftClassDependency(TestDummyNoncycleA)
class TestDummyNoncycleB {

    @Test(groups = MethodSorterTest.DUMMY)
    @UnitTestId(8)
    void testDummyNoncycleClassB() {}

}
