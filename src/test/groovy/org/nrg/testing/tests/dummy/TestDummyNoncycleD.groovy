package org.nrg.testing.tests.dummy

import org.nrg.testing.unit.UnitTestId
import org.nrg.testing.annotations.SoftClassDependency
import org.nrg.testing.tests.MethodSorterTest
import org.testng.annotations.Test

@SoftClassDependency([TestDummyNoncycleB, TestDummyNoncycleC, TestDummyNoncycleE])
class TestDummyNoncycleD {

    @Test(groups = MethodSorterTest.DUMMY)
    @UnitTestId(8)
    void testDummyNoncycleClassD() {}

}
