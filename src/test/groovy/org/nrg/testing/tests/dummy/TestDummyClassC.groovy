package org.nrg.testing.tests.dummy

import org.nrg.testing.UnitTestId
import org.nrg.testing.annotations.SoftClassDependency
import org.nrg.testing.tests.MethodSorterTest
import org.testng.annotations.Test

@SoftClassDependency(TestDummyClassC)
class TestDummyClassC {

    @Test(groups = MethodSorterTest.DUMMY)
    @UnitTestId(7)
    void testDummyClassC() {}

}
