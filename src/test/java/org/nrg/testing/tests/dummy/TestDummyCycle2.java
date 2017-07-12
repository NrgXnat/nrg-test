package org.nrg.testing.tests.dummy;

import org.nrg.testing.UnitTestId;
import org.nrg.testing.annotations.SoftClassDependency;
import org.testng.annotations.Test;

@SoftClassDependency({TestDummyCycle0.class})
public class TestDummyCycle2 {

    @Test(groups = "dummy")
    @UnitTestId(9)
    public void testDummyCycleClass2() {}

}
