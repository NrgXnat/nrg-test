/*
 * org.nrg.xnat.selenium.util.RandomHelper
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 2/4/14 11:19 AM
 */

package org.nrg.testing.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Precision;
import org.nrg.xnat.enums.Month;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.apache.commons.lang3.RandomStringUtils.*;

public class RandomHelper {

    private static final Random generator = new Random();

    private RandomHelper() {}

    /**
     * Returns a random string of length 14 which does not start with a number
     * @return randomized String
     */
    public static String randomID() {
        return randomID(14);
    }

    /**
     * returns a random string of arbitrary length which does not start with a number
     * @param length length of returned String
     * @return randomized String
     */
    public static String randomID(int length) {
        return randomAlphabetic(1) + randomAlphanumeric(length - 1);
    }

    public static String smallRandomID() {
        return randomID(8);
    }

    /**
     * Generates a random alphanumeric string of arbitrary length
     * @param length length of returned String
     * @return randomized String
     */
    public static String randomString(int length) {
        return randomAlphanumeric(length);
    }

    /**
     * Generates lines of random alphanumeric strings separated by \n
     * @param lengthPerLine Number of chars per line
     * @param numLines Number of lines to generate
     * @return String of lines
     */
    public static String randomStringLines(int lengthPerLine, int numLines) {
        final List<String> lines = new ArrayList<>();

        for (int i = 0; i < numLines; i++) {
            lines.add(randomString(lengthPerLine));
        }

        return StringUtils.join(lines, "\n");
    }

    /**
     * Generates a random string from a large list of characters of arbitrary length
     * @param length length of randomized string
     * @return randomized String
     */
    public static String randomFullString(int length) {
        return random(length, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789`-=;',./~!@#$%^&*()_+{}|:<>?");
    }

    /**
     * Generates a random string of letters of arbitrary length
     * @param length length of returned string
     * @return randomized String
     */
    public static String randomLetters(int length) {
        return randomAlphabetic(length);
    }

    /**
     * Generates 0 periods or 1 period, each with 1/2 probability
     */
    public static String randomPeriods() {
        return (generator.nextBoolean()) ? "" : ".";
    }

    /**
     * returns a random integer in [lowerBound, upperBound]
     * @param lowerBound lower end of range
     * @param upperBound upper end of range
     * @return random int
     */
    public static int randomInteger(int lowerBound, int upperBound) {
        return (generator.nextInt(upperBound + 1 - lowerBound) + lowerBound);
    }

    public static double randomDouble(int decimalPlaces) {
        // returns a double between 0 and 1

        return Precision.round(generator.nextDouble(), decimalPlaces);
    }

    /**
     * Returns a list of random integers
     * @param numbersToGenerate    The total numbers to be generated
     * @param lowerBound           The smallest possible value (inclusive)
     * @param upperBound           The largest possible value (inclusive)
     * @param withReplacement      Should the method execute with replacement? That is, once an integer is chosen, can it be chosen again?
     * @return                     A list of Integers with the generated integers
     */
    public static List<Integer> randomIntegers(int numbersToGenerate, int lowerBound, int upperBound, boolean withReplacement) {
        List<Integer> randomIntegers = new ArrayList<>();
        ArrayList<Integer> sourceList = new ArrayList<>();

        for (int i = lowerBound; i <= upperBound; i++) {
            sourceList.add(i);
        }

        for (int i = 0; i < numbersToGenerate; i++) {
            int val = sourceList.get(generator.nextInt(sourceList.size()));
            randomIntegers.add(val);
            if (!withReplacement) {
                sourceList.remove(new Integer(val));
            }
        }
        return randomIntegers;
    }

    /**
     * returns "Male" or "Female" with equal probability
     * @return gender
     */
    public static String randomGender() {
        return generator.nextBoolean() ? "Male" : "Female";
    }

    /**
     * returns "Left" or "Right" with equal probability
     * @return handedness
     */
    public static String randomHandedness() {
        return generator.nextBoolean() ? "Right" : "Left";
    }

    public static <T extends Enum<?>> T randomEnum(Class<T> enumClass) {
        return enumClass.getEnumConstants()[generator.nextInt(enumClass.getEnumConstants().length)];
    }

    public static Month randomMonth() {
        return randomEnum(Month.class);
    }

    public static int randomDay() {
        return randomInteger(1, 28);
    }

    public static int randomYear() {
        return randomInteger(1990, 2017);
    }

    public static <T> T randomListEntry(List<T> list) {
        return list.get(RandomHelper.randomInteger(0, list.size() - 1));
    }

}
