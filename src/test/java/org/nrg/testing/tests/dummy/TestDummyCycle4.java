package org.nrg.testing.tests.dummy;

import org.nrg.testing.UnitTestId;
import org.nrg.testing.annotations.SoftClassDependency;
import org.testng.annotations.Test;

@SoftClassDependency({TestDummyCycle3.class})
public class TestDummyCycle4 {

    @Test(groups = "dummy")
    @UnitTestId(9)
    public void testDummyCycleClass4() {}

}
