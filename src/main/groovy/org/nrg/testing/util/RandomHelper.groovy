package org.nrg.testing.util

import org.apache.commons.math3.util.Precision

import static org.apache.commons.lang3.RandomStringUtils.*

@SuppressWarnings('unused')
class RandomHelper {

    private static final Random generator = new Random()

    private RandomHelper() {}

    /**
     * Returns a random string of length 14 which does not start with a number
     * @return randomized String
     */
    static String randomID() {
        randomID(14)
    }

    /**
     * returns a random string of arbitrary length which does not start with a number
     * @param  length length of returned String
     * @return randomized String
     */
    static String randomID(int length) {
        randomAlphabetic(1) + randomAlphanumeric(length - 1)
    }

    static String smallRandomID() {
        randomID(8)
    }

    /**
     * Generates a random alphanumeric string of arbitrary length
     * @param  length length of returned String
     * @return randomized String
     */
    static String randomString(int length) {
        randomAlphanumeric(length)
    }

    /**
     * Generates lines of random alphanumeric strings separated by \n
     * @param lengthPerLine Number of chars per line
     * @param numLines      Number of lines to generate
     * @return              String of lines
     */
    static String randomStringLines(int lengthPerLine, int numLines) {
        (0 ..< numLines).collect {
            randomString(lengthPerLine)
        }.join('\n')
    }

    /**
     * Generates a random string from a large list of characters of arbitrary length
     * @param  length length of randomized string
     * @return randomized String
     */
    static String randomFullString(int length) {
        random(length, 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789`-=\',./~!@#$%^&*()_+{}|:<>?')
    }

    /**
     * Generates a random string of letters of arbitrary length
     * @param  length length of returned string
     * @return randomized String
     */
    static String randomLetters(int length) {
        randomAlphabetic(length)
    }

    /**
     * returns a random integer in [lowerBound, upperBound]
     * @param lowerBound lower end of range
     * @param upperBound upper end of range
     * @return random int
     */
    static int randomInteger(int lowerBound, int upperBound) {
        generator.nextInt(upperBound + 1 - lowerBound) + lowerBound
    }

    /**
     * returns a random double between 0 and 1
     * @param  decimalPlaces number of decimal places in the random double
     * @return               random double
     */
    static double randomDouble(int decimalPlaces) {
        Precision.round(generator.nextDouble(), decimalPlaces)
    }

    /**
     * Returns a list of random integers
     * @param numbersToGenerate    The total numbers to be generated
     * @param lowerBound           The smallest possible value (inclusive)
     * @param upperBound           The largest possible value (inclusive)
     * @param withReplacement      Should the method execute with replacement? That is, once an integer is chosen, can it be chosen again?
     * @return                     A list of Integers with the generated integers
     */
    static List<Integer> randomIntegers(int numbersToGenerate, int lowerBound, int upperBound, boolean withReplacement) {
        final int spaceSize = upperBound - lowerBound + 1
        if (!withReplacement && numbersToGenerate > spaceSize) {
            throw new UnsupportedOperationException("Not possible to generate ${numbersToGenerate} random integers from a list of size ${spaceSize} without replacement.")
        }

        final List<Integer> sourceList = (lowerBound .. upperBound).toList()
        final List<Integer> randomIntegers = []

        numbersToGenerate.times {
            final int randomInteger = randomListEntry(sourceList)
            randomIntegers << randomInteger
            if (!withReplacement) {
                sourceList.removeElement(randomInteger)
            }
        }
        randomIntegers
    }

    /**
     * returns 'Male' or 'Female' with equal probability
     * @return gender
     */
    static String randomGender() {
        generator.nextBoolean() ? 'Male' : 'Female'
    }

    /**
     * returns 'Left' or 'Right' with equal probability
     * @return handedness
     */
    static String randomHandedness() {
        generator.nextBoolean() ? 'Right' : 'Left'
    }

    static <T extends Enum> T randomEnum(Class<T> enumClass) {
        randomListEntry(enumClass.getEnumConstants() as List<T>)
    }

    static int randomMonth() {
        randomInteger(1, 12)
    }

    static int randomDay() {
        randomInteger(1, 28)
    }

    static int randomYear() {
        randomInteger(1990, 2017)
    }

    static <T> T randomListEntry(List<T> list) {
        list.get(randomInteger(0, list.size() - 1))
    }

}
