package org.nrg.testing.tests.dummy

import org.nrg.testing.unit.UnitTestId
import org.nrg.testing.annotations.HardDependency
import org.nrg.testing.tests.MethodSorterTest
import org.testng.annotations.Test

@Test(groups = MethodSorterTest.DUMMY)
class TestDummyClassA {

    @UnitTestId(1)
    void testSingleDummyTest() {}

    @UnitTestId(2)
    void testDummyA1() {}

    @UnitTestId(2)
    void testDummyA2() {}

    @UnitTestId(2)
    void testDummyA3() {}

    @UnitTestId(3)
    @HardDependency('testDummy1Cycle')
    void testDummy1Cycle() {}
    
    @UnitTestId(4)
    @HardDependency('testDummy2CycleB')
    void testDummy2CycleA() {}

    @UnitTestId(4)
    @HardDependency('testDummy2CycleA')
    void testDummy2CycleB() {}
    
    @UnitTestId(5)
    void testDummy4CycleA() {}

    @UnitTestId(5)
    @HardDependency(['testDummy4CycleA', 'testDummy4CycleD'])
    void testDummy4CycleB() {}
    
    @UnitTestId(5)
    @HardDependency(['testDummy4CycleB'])
    void testDummy4CycleC() {}

    @UnitTestId(5)
    @HardDependency(['testDummy4CycleE'])
    void testDummy4CycleD() {}

    @UnitTestId(5)
    @HardDependency(['testDummy4CycleC'])
    void testDummy4CycleE() {}

    @UnitTestId(5)
    @HardDependency(['testDummy4CycleE'])
    void testDummy4CycleF() {}
    
    @UnitTestId(6)
    void testDummyNoncycle1() {}

    @UnitTestId(6)
    @HardDependency('testDummyNoncycle1')
    void testDummyNoncycle2() {}

    @UnitTestId(6)
    @HardDependency(['testDummyNoncycle2', 'testDummyNoncycle6'])
    void testDummyNoncycle3() {}

    @UnitTestId(6)
    @HardDependency(['testDummyNoncycle2'])
    void testDummyNoncycle4() {}

    @UnitTestId(6)
    @HardDependency(['testDummyNoncycle4'])
    void testDummyNoncycle5() {}

    @UnitTestId(6)
    @HardDependency(['testDummyNoncycle4', 'testDummyNoncycle5'])
    void testDummyNoncycle6() {}

}
