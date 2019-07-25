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
        UnitTestUtils.assertDoubleEqual(COLOR_PIXEL1.calculateDistance(TAXICAB), 18)
        UnitTestUtils.assertDoubleEqual(COLOR_PIXEL2.calculateDistance(TAXICAB), 19)
        UnitTestUtils.assertDoubleEqual(GRAY_PIXEL1.calculateDistance(TAXICAB), 100)
        UnitTestUtils.assertDoubleEqual(GRAY_PIXEL2.calculateDistance(TAXICAB), 1)
        UnitTestUtils.assertDoubleEqual(GRAY_ZERO_PIXEL.calculateDistance(TAXICAB), 0)
        UnitTestUtils.assertDoubleEqual(GRAY_ZERO_DIFF_PIXEL.calculateDistance(TAXICAB), 0)
        UnitTestUtils.assertDoubleEqual(COLOR_ZERO_PIXEL.calculateDistance(TAXICAB), 0)
        UnitTestUtils.assertDoubleEqual(COLOR_ZERO_DIFF_PIXEL.calculateDistance(TAXICAB), 0)
    }

    @Test
    void test2Norm() {
        UnitTestUtils.assertDoubleEqual(COLOR_PIXEL1.calculateDistance(EUCLIDEAN), 3*Math.sqrt(14))
        UnitTestUtils.assertDoubleEqual(COLOR_PIXEL2.calculateDistance(EUCLIDEAN), 5*Math.sqrt(13))
        UnitTestUtils.assertDoubleEqual(GRAY_PIXEL1.calculateDistance(EUCLIDEAN), 100)
        UnitTestUtils.assertDoubleEqual(GRAY_PIXEL2.calculateDistance(EUCLIDEAN), 1)
        UnitTestUtils.assertDoubleEqual(GRAY_ZERO_PIXEL.calculateDistance(EUCLIDEAN), 0)
        UnitTestUtils.assertDoubleEqual(GRAY_ZERO_DIFF_PIXEL.calculateDistance(EUCLIDEAN), 0)
        UnitTestUtils.assertDoubleEqual(COLOR_ZERO_PIXEL.calculateDistance(EUCLIDEAN), 0)
        UnitTestUtils.assertDoubleEqual(COLOR_ZERO_DIFF_PIXEL.calculateDistance(EUCLIDEAN), 0)
    }

    @Test
    void testDiscreteMetric() {
        UnitTestUtils.assertDoubleEqual(COLOR_PIXEL1.calculateDistance(DISCRETE), 1)
        UnitTestUtils.assertDoubleEqual(COLOR_PIXEL2.calculateDistance(DISCRETE), 1)
        UnitTestUtils.assertDoubleEqual(GRAY_PIXEL1.calculateDistance(DISCRETE), 1)
        UnitTestUtils.assertDoubleEqual(GRAY_PIXEL2.calculateDistance(DISCRETE), 1)
        UnitTestUtils.assertDoubleEqual(GRAY_ZERO_PIXEL.calculateDistance(DISCRETE), 0)
        UnitTestUtils.assertDoubleEqual(GRAY_ZERO_DIFF_PIXEL.calculateDistance(DISCRETE), 0)
        UnitTestUtils.assertDoubleEqual(COLOR_ZERO_PIXEL.calculateDistance(DISCRETE), 0)
        UnitTestUtils.assertDoubleEqual(COLOR_ZERO_DIFF_PIXEL.calculateDistance(DISCRETE), 0)
    }

    @Test
    void testHammingMetric() {
        UnitTestUtils.assertDoubleEqual(COLOR_PIXEL1.calculateDistance(HAMMING), 3)
        UnitTestUtils.assertDoubleEqual(COLOR_PIXEL2.calculateDistance(HAMMING), 2)
        UnitTestUtils.assertDoubleEqual(GRAY_PIXEL1.calculateDistance(HAMMING), 1)
        UnitTestUtils.assertDoubleEqual(GRAY_PIXEL2.calculateDistance(HAMMING), 1)
        UnitTestUtils.assertDoubleEqual(GRAY_ZERO_PIXEL.calculateDistance(HAMMING), 0)
        UnitTestUtils.assertDoubleEqual(GRAY_ZERO_DIFF_PIXEL.calculateDistance(HAMMING), 0)
        UnitTestUtils.assertDoubleEqual(COLOR_ZERO_PIXEL.calculateDistance(HAMMING), 0)
        UnitTestUtils.assertDoubleEqual(COLOR_ZERO_DIFF_PIXEL.calculateDistance(HAMMING), 0)
    }

}