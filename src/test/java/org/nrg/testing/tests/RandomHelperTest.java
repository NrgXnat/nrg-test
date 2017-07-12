package org.nrg.testing.tests;

import com.google.common.collect.Sets;
import org.nrg.testing.util.RandomHelper;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

import static org.testng.AssertJUnit.assertEquals;

public class RandomHelperTest {

    private final int randomTests = 1000;

    @Test
    public void testRandomInteger() {
        final int lowerBound = 1;
        final int upperBound = 6;
        final Set<Integer> dice = Sets.newHashSet(1, 2, 3, 4, 5, 6);
        final Set<Integer> generated = new HashSet<>();

        for (int i = 0; i < randomTests; i++) {
            generated.add(RandomHelper.randomInteger(lowerBound, upperBound));
        }

        assertEquals(dice, generated); // technically speaking, this doesn't HAVE to be true for the function to be working correct. However, probabilistically speaking, it's essentially going to always be true.
    }

}
