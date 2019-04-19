package org.nrg.testing;

import org.nrg.testing.file.FileLocation;
import org.testng.Assert;

public class UnitTestUtils {

    private static final double TOLERANCE = 0.000001;
    public static final String DATA_LOCATION = FileLocation.getDataLocation();

    public static void assertDoubleEqual(double actual, double expected) {
        Assert.assertEquals(actual, expected, TOLERANCE);
    }

}
