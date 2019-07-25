package org.nrg.testing.tests

import org.nrg.testing.util.RandomHelper
import org.testng.annotations.Test

import static org.testng.AssertJUnit.*

/**
 * Technically speaking, many of the functions could be working correctly and the tests could still fail if some items are not generated. However, probabilistically speaking, that's [virtually] never going to happen by having GENERATION_COUNT be large
 */
class RandomHelperTest {

    private static final int DICE_LOWER_BOUND = 1
    private static final int DICE_UPPER_BOUND = 6
    private static final Set<Integer> DICE = DICE_LOWER_BOUND .. DICE_UPPER_BOUND
    private static final int GENERATION_COUNT = 1000

    @Test
    void testRandomInteger() {
        assertEquals(DICE, (0 ..< GENERATION_COUNT).collect { RandomHelper.randomInteger(DICE_LOWER_BOUND, DICE_UPPER_BOUND) } as Set<Integer>)
    }

    @Test
    void testRandomIntegersWithReplacement() {
        final List<Integer> aThousandDice = RandomHelper.randomIntegers(GENERATION_COUNT, DICE_LOWER_BOUND, DICE_UPPER_BOUND, true)
        assertEquals(GENERATION_COUNT, aThousandDice.size())
        DICE.each { possibleRoll ->
            assertTrue(aThousandDice.count { it == possibleRoll } > GENERATION_COUNT / 100) // e.g. with 1000 rolls, I better have gotten each number at least 10 times!
        }
    }

    @Test
    void testRandomIntegersWithoutReplacement() {
        final int lowerBound = 100
        final int upperBound = lowerBound + GENERATION_COUNT - 1
        final List<Integer> fullList = lowerBound .. upperBound
        final List<Integer> randomized = RandomHelper.randomIntegers(fullList.size(), lowerBound, upperBound, false) // essentially generating a permutation
        assertEquals(fullList as Set, randomized as Set) // should have the same elements
        assertFalse(fullList == randomized) // but not the same order
        assertEquals(GENERATION_COUNT, randomized.size())
    }

    @Test(expectedExceptions = UnsupportedOperationException)
    void testRandomIntegersInsufficientList() {
        RandomHelper.randomIntegers(DICE.size() + 1, DICE_LOWER_BOUND, DICE_UPPER_BOUND, false)
    }

}
