package org.nrg.testing.tests.dummy;

import org.nrg.testing.UnitTestId;
import org.nrg.testing.annotations.SoftClassDependency;
import org.testng.annotations.Test;

@SoftClassDependency({TestDummyCycle2.class})
public class TestDummyCycle1 {

    @Test(groups = "dummy")
    @UnitTestId(9)
    public void testDummyCycleClass1() {}

}
