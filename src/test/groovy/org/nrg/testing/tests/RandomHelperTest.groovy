package org.nrg.testing.tests

import org.nrg.testing.util.RandomHelper
import org.testng.annotations.Test

import static org.testng.AssertJUnit.assertEquals

class RandomHelperTest {

    private static final int GENERATION_COUNT = 1000

    @Test
    void testRandomInteger() {
        final int lowerBound = 1
        final int upperBound = 6
        final Set<Integer> dice = 1 .. 6
        final Set<Integer> generated = []

        GENERATION_COUNT.times { generated << RandomHelper.randomInteger(lowerBound, upperBound) }

        assertEquals(dice, generated) // technically speaking, this doesn't HAVE to be true for the function to be working correct. However, probabilistically speaking, it's essentially going to always be true.
    }

}
