package org.nrg.testing.tests.dummy

import org.nrg.testing.annotations.SoftDependency
import org.nrg.testing.annotations.SortLast
import org.nrg.testing.tests.MethodSorterTest
import org.nrg.testing.unit.UnitTestId
import org.testng.annotations.Test

@Test(groups = MethodSorterTest.DUMMY)
class TestDummySortLast {

    @UnitTestId([10, 11])
    @SortLast
    void testDummyLast() {}

    @UnitTestId(10)
    @SortLast
    @SoftDependency('testDummyLast')
    void testDummyLastWithDependency() {}

    @UnitTestId(10)
    void testDummyGeneric() {}

    @UnitTestId(11)
    @SoftDependency('testDummyLast')
    void testDummyInvalidLastDependency() {}

}
