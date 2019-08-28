package org.nrg.testing.tests.dummy

import org.nrg.testing.unit.UnitTestId
import org.nrg.testing.annotations.HardDependency
import org.nrg.testing.tests.MethodSorterTest
import org.testng.annotations.Test

class TestDummyClassA {

    @Test(groups = MethodSorterTest.DUMMY)
    @UnitTestId(1)
    void testSingleDummyTest() {}

    @Test(groups = MethodSorterTest.DUMMY)
    @UnitTestId(2)
    void testDummyA1() {}

    @Test(groups = MethodSorterTest.DUMMY)
    @UnitTestId(2)
    void testDummyA2() {}

    @Test(groups = MethodSorterTest.DUMMY)
    @UnitTestId(2)
    void testDummyA3() {}

    @Test(groups = MethodSorterTest.DUMMY)
    @UnitTestId(3)
    @HardDependency('testDummy1Cycle')
    void testDummy1Cycle() {}

    @Test(groups = MethodSorterTest.DUMMY)
    @UnitTestId(4)
    @HardDependency('testDummy2CycleB')
    void testDummy2CycleA() {}

    @Test(groups = MethodSorterTest.DUMMY)
    @UnitTestId(4)
    @HardDependency('testDummy2CycleA')
    void testDummy2CycleB() {}

    @Test(groups = MethodSorterTest.DUMMY)
    @UnitTestId(5)
    void testDummy4CycleA() {}

    @Test(groups = MethodSorterTest.DUMMY)
    @UnitTestId(5)
    @HardDependency(['testDummy4CycleA', 'testDummy4CycleD'])
    void testDummy4CycleB() {}

    @Test(groups = MethodSorterTest.DUMMY)
    @UnitTestId(5)
    @HardDependency(['testDummy4CycleB'])
    void testDummy4CycleC() {}

    @Test(groups = MethodSorterTest.DUMMY)
    @UnitTestId(5)
    @HardDependency(['testDummy4CycleE'])
    void testDummy4CycleD() {}

    @Test(groups = MethodSorterTest.DUMMY)
    @UnitTestId(5)
    @HardDependency(['testDummy4CycleC'])
    void testDummy4CycleE() {}

    @Test(groups = MethodSorterTest.DUMMY)
    @UnitTestId(5)
    @HardDependency(['testDummy4CycleE'])
    void testDummy4CycleF() {}

    @Test(groups = MethodSorterTest.DUMMY)
    @UnitTestId(6)
    void testDummyNoncycle1() {}

    @Test(groups = MethodSorterTest.DUMMY)
    @UnitTestId(6)
    @HardDependency('testDummyNoncycle1')
    void testDummyNoncycle2() {}

    @Test(groups = MethodSorterTest.DUMMY)
    @UnitTestId(6)
    @HardDependency(['testDummyNoncycle2', 'testDummyNoncycle6'])
    void testDummyNoncycle3() {}

    @Test(groups = MethodSorterTest.DUMMY)
    @UnitTestId(6)
    @HardDependency(['testDummyNoncycle2'])
    void testDummyNoncycle4() {}

    @Test(groups = MethodSorterTest.DUMMY)
    @UnitTestId(6)
    @HardDependency(['testDummyNoncycle4'])
    void testDummyNoncycle5() {}

    @Test(groups = MethodSorterTest.DUMMY)
    @UnitTestId(6)
    @HardDependency(['testDummyNoncycle4', 'testDummyNoncycle5'])
    void testDummyNoncycle6() {}

}
