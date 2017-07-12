package org.nrg.testing.tests.dummy;

import org.nrg.testing.UnitTestId;
import org.nrg.testing.annotations.SoftClassDependency;
import org.testng.annotations.Test;

@SoftClassDependency(TestDummyNoncycleA.class)
public class TestDummyNoncycleB {

    @Test(groups = "dummy")
    @UnitTestId(8)
    public void testDummyNoncycleClassB() {}

}
