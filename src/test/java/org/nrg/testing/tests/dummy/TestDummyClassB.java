package org.nrg.testing.tests.dummy;

import org.nrg.testing.UnitTestId;
import org.testng.annotations.Test;

public class TestDummyClassB {

    @Test(groups = "dummy")
    @UnitTestId(2)
    public void testDummyB1() {}

    @Test(groups = "dummy")
    @UnitTestId(2)
    public void testDummyB2() {}

    @Test(groups = "dummy")
    @UnitTestId(6)
    public void testDummyNoncycleA() {}

    @Test(groups = "dummy")
    @UnitTestId(6)
    public void testDummyNoncycleD() {}

    @Test(groups = "dummy")
    @UnitTestId(6)
    public void testDummyNoncycleC() {}

    @Test(groups = "dummy")
    @UnitTestId(6)
    public void testDummyNoncycleB() {}

}
