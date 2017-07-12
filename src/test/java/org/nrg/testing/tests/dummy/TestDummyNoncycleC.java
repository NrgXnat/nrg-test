package org.nrg.testing.tests.dummy;

import org.nrg.testing.UnitTestId;
import org.nrg.testing.annotations.SoftClassDependency;
import org.testng.annotations.Test;

@SoftClassDependency(TestDummyNoncycleB.class)
public class TestDummyNoncycleC {

    @Test(groups = "dummy")
    @UnitTestId(8)
    public void testDummyNoncycleClassC() {}

}
