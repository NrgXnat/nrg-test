package org.nrg.testing.tests

import org.nrg.testing.UnitTestUtils
import org.nrg.testing.xnat.processing.files.comparators.imaging.ComparisonPixel
import org.nrg.testing.xnat.processing.files.comparators.imaging.PixelFactory
import org.testng.annotations.Test

import static org.nrg.testing.xnat.processing.files.comparators.imaging.metrics.Metrics.*

class ComparisonPixelTest {

    private static final ComparisonPixel COLOR_PIXEL1 = PixelFactory.getPixel(3, 6, 9, 0, 0, 0)
    private static final ComparisonPixel COLOR_PIXEL2 = PixelFactory.getPixel(4, -9, -2, 3, -9, -20)
    private static final ComparisonPixel GRAY_PIXEL1 = PixelFactory.getPixel(100, 0)
    private static final ComparisonPixel GRAY_PIXEL2 = PixelFactory.getPixel(99, 100)
    private static final ComparisonPixel GRAY_ZERO_PIXEL = PixelFactory.getPixel(77, 77)
    private static final ComparisonPixel GRAY_ZERO_DIFF_PIXEL = PixelFactory.getPixel(100, 100, true)
    private static final ComparisonPixel COLOR_ZERO_PIXEL = PixelFactory.getPixel(77, 76, 75, 77, 76, 75)
    private static final ComparisonPixel COLOR_ZERO_DIFF_PIXEL = PixelFactory.getPixel(5, 8, 13, 5, 8, 13)

    @Test
    void test1Norm() {
        UnitTestUtils.assertDoubleEqual(18, COLOR_PIXEL1.calculateDistance(TAXICAB))
        UnitTestUtils.assertDoubleEqual(19, COLOR_PIXEL2.calculateDistance(TAXICAB))
        UnitTestUtils.assertDoubleEqual(100, GRAY_PIXEL1.calculateDistance(TAXICAB))
        UnitTestUtils.assertDoubleEqual(1, GRAY_PIXEL2.calculateDistance(TAXICAB))
        UnitTestUtils.assertDoubleEqual(0, GRAY_ZERO_PIXEL.calculateDistance(TAXICAB))
        UnitTestUtils.assertDoubleEqual(0, GRAY_ZERO_DIFF_PIXEL.calculateDistance(TAXICAB))
        UnitTestUtils.assertDoubleEqual(0, COLOR_ZERO_PIXEL.calculateDistance(TAXICAB))
        UnitTestUtils.assertDoubleEqual(0, COLOR_ZERO_DIFF_PIXEL.calculateDistance(TAXICAB))
    }

    @Test
    void test2Norm() {
        UnitTestUtils.assertDoubleEqual(3 * Math.sqrt(14), COLOR_PIXEL1.calculateDistance(EUCLIDEAN))
        UnitTestUtils.assertDoubleEqual(5 * Math.sqrt(13), COLOR_PIXEL2.calculateDistance(EUCLIDEAN))
        UnitTestUtils.assertDoubleEqual(100, GRAY_PIXEL1.calculateDistance(EUCLIDEAN))
        UnitTestUtils.assertDoubleEqual(1, GRAY_PIXEL2.calculateDistance(EUCLIDEAN))
        UnitTestUtils.assertDoubleEqual(0, GRAY_ZERO_PIXEL.calculateDistance(EUCLIDEAN))
        UnitTestUtils.assertDoubleEqual(0, GRAY_ZERO_DIFF_PIXEL.calculateDistance(EUCLIDEAN))
        UnitTestUtils.assertDoubleEqual(0, COLOR_ZERO_PIXEL.calculateDistance(EUCLIDEAN))
        UnitTestUtils.assertDoubleEqual(0, COLOR_ZERO_DIFF_PIXEL.calculateDistance(EUCLIDEAN))
    }

    @Test
    void testDiscreteMetric() {
        UnitTestUtils.assertDoubleEqual(1, COLOR_PIXEL1.calculateDistance(DISCRETE))
        UnitTestUtils.assertDoubleEqual(1, COLOR_PIXEL2.calculateDistance(DISCRETE))
        UnitTestUtils.assertDoubleEqual(1, GRAY_PIXEL1.calculateDistance(DISCRETE))
        UnitTestUtils.assertDoubleEqual(1, GRAY_PIXEL2.calculateDistance(DISCRETE))
        UnitTestUtils.assertDoubleEqual(0, GRAY_ZERO_PIXEL.calculateDistance(DISCRETE))
        UnitTestUtils.assertDoubleEqual(0, GRAY_ZERO_DIFF_PIXEL.calculateDistance(DISCRETE))
        UnitTestUtils.assertDoubleEqual(0, COLOR_ZERO_PIXEL.calculateDistance(DISCRETE))
        UnitTestUtils.assertDoubleEqual(0, COLOR_ZERO_DIFF_PIXEL.calculateDistance(DISCRETE))
    }

    @Test
    void testHammingMetric() {
        UnitTestUtils.assertDoubleEqual(3, COLOR_PIXEL1.calculateDistance(HAMMING))
        UnitTestUtils.assertDoubleEqual(2, COLOR_PIXEL2.calculateDistance(HAMMING))
        UnitTestUtils.assertDoubleEqual(1, GRAY_PIXEL1.calculateDistance(HAMMING))
        UnitTestUtils.assertDoubleEqual(1, GRAY_PIXEL2.calculateDistance(HAMMING))
        UnitTestUtils.assertDoubleEqual(0, GRAY_ZERO_PIXEL.calculateDistance(HAMMING))
        UnitTestUtils.assertDoubleEqual(0, GRAY_ZERO_DIFF_PIXEL.calculateDistance(HAMMING))
        UnitTestUtils.assertDoubleEqual(0, COLOR_ZERO_PIXEL.calculateDistance(HAMMING))
        UnitTestUtils.assertDoubleEqual(0, COLOR_ZERO_DIFF_PIXEL.calculateDistance(HAMMING))
    }

}