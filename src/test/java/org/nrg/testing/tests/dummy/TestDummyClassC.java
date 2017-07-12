package org.nrg.testing.tests.dummy;

import org.nrg.testing.UnitTestId;
import org.nrg.testing.annotations.HardDependency;
import org.nrg.testing.annotations.SoftClassDependency;
import org.testng.annotations.Test;

@SoftClassDependency(TestDummyClassC.class)
public class TestDummyClassC {

    @Test(groups = "dummy")
    @UnitTestId(7)
    public void testDummyClassC() {}

}
