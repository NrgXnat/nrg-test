package org.nrg.testing

import org.nrg.testing.file.FileLocation
import org.testng.Assert

class UnitTestUtils {

    private static final double TOLERANCE = 0.000001
    public static final String DATA_LOCATION = FileLocation.getDataLocation()

    static void assertDoubleEqual(double expected, double actual) {
        Assert.assertEquals(actual, expected, TOLERANCE)
    }

}
