package org.nrg.testing.tests.dummy

import org.nrg.testing.annotations.HardDependency
import org.nrg.testing.tests.MethodSorterTest
import org.nrg.testing.unit.UnitTestId
import org.testng.annotations.Test

@Test(groups = MethodSorterTest.DUMMY)
class TestDummyMissingDeps {

    void testDummyMissingDepMethod() {}

    @UnitTestId(12)
    @HardDependency('testDummyMissingDepMethod')
    void testDummyMethodWithMissingDep() {}

}
