package org.nrg.testing.tests.dummy

import org.nrg.testing.unit.UnitTestId
import org.nrg.testing.annotations.SoftClassDependency
import org.nrg.testing.tests.MethodSorterTest
import org.testng.annotations.Test

@SoftClassDependency(TestDummyNoncycleA)
@Test(groups = MethodSorterTest.DUMMY)
class TestDummyNoncycleB {

    @UnitTestId(8)
    void testDummyNoncycleClassB() {}

}
