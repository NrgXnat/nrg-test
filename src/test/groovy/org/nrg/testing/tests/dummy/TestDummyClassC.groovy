package org.nrg.testing.tests.dummy

import org.nrg.testing.unit.UnitTestId
import org.nrg.testing.annotations.SoftClassDependency
import org.nrg.testing.tests.MethodSorterTest
import org.testng.annotations.Test

@SoftClassDependency(TestDummyClassC)
@Test(groups = MethodSorterTest.DUMMY)
class TestDummyClassC {

    @UnitTestId(7)
    void testDummyClassC() {}

}
