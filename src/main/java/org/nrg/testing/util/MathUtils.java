package org.nrg.testing.util;

public class MathUtils {

    public static double percentError(double expected, double actual) {
        return 100*Math.abs((actual - expected)/expected);
    }

}
