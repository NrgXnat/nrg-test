package org.nrg.testing.tests.dummy;

import org.nrg.testing.UnitTestId;
import org.nrg.testing.annotations.HardDependency;
import org.testng.annotations.Test;

public class TestDummyClassA {

    @Test(groups = "dummy")
    @UnitTestId(1)
    public void testSingleDummyTest() {}

    @Test(groups = "dummy")
    @UnitTestId(2)
    public void testDummyA1() {}

    @Test(groups = "dummy")
    @UnitTestId(2)
    public void testDummyA2() {}

    @Test(groups = "dummy")
    @UnitTestId(2)
    public void testDummyA3() {}

    @Test(groups = "dummy")
    @UnitTestId(3)
    @HardDependency("testDummy1Cycle")
    public void testDummy1Cycle() {}

    @Test(groups = "dummy")
    @UnitTestId(4)
    @HardDependency("testDummy2CycleB")
    public void testDummy2CycleA() {}

    @Test(groups = "dummy")
    @UnitTestId(4)
    @HardDependency("testDummy2CycleA")
    public void testDummy2CycleB() {}

    @Test(groups = "dummy")
    @UnitTestId(5)
    public void testDummy4CycleA() {}

    @Test(groups = "dummy")
    @UnitTestId(5)
    @HardDependency({"testDummy4CycleA", "testDummy4CycleD"})
    public void testDummy4CycleB() {}

    @Test(groups = "dummy")
    @UnitTestId(5)
    @HardDependency({"testDummy4CycleB"})
    public void testDummy4CycleC() {}

    @Test(groups = "dummy")
    @UnitTestId(5)
    @HardDependency({"testDummy4CycleE"})
    public void testDummy4CycleD() {}

    @Test(groups = "dummy")
    @UnitTestId(5)
    @HardDependency({"testDummy4CycleC"})
    public void testDummy4CycleE() {}

    @Test(groups = "dummy")
    @UnitTestId(5)
    @HardDependency({"testDummy4CycleE"})
    public void testDummy4CycleF() {}

    @Test(groups = "dummy")
    @UnitTestId(6)
    public void testDummyNoncycle1() {}

    @Test(groups = "dummy")
    @UnitTestId(6)
    @HardDependency("testDummyNoncycle1")
    public void testDummyNoncycle2() {}

    @Test(groups = "dummy")
    @UnitTestId(6)
    @HardDependency({"testDummyNoncycle2", "testDummyNoncycle6"})
    public void testDummyNoncycle3() {}

    @Test(groups = "dummy")
    @UnitTestId(6)
    @HardDependency({"testDummyNoncycle2"})
    public void testDummyNoncycle4() {}

    @Test(groups = "dummy")
    @UnitTestId(6)
    @HardDependency({"testDummyNoncycle4"})
    public void testDummyNoncycle5() {}

    @Test(groups = "dummy")
    @UnitTestId(6)
    @HardDependency({"testDummyNoncycle4", "testDummyNoncycle5"})
    public void testDummyNoncycle6() {}

}
