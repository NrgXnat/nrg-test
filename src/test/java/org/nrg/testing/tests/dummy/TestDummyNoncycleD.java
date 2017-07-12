package org.nrg.testing.tests.dummy;

import org.nrg.testing.UnitTestId;
import org.nrg.testing.annotations.SoftClassDependency;
import org.testng.annotations.Test;

@SoftClassDependency({TestDummyNoncycleB.class, TestDummyNoncycleC.class, TestDummyNoncycleE.class})
public class TestDummyNoncycleD {

    @Test(groups = "dummy")
    @UnitTestId(8)
    public void testDummyNoncycleClassD() {}

}
