package org.nrg.testing.tests;

import org.nrg.testing.UnitTestUtils;
import org.nrg.testing.xnat.processing.files.comparators.imaging.ComparisonPixel;
import org.nrg.testing.xnat.processing.files.comparators.imaging.PNormDistance;
import org.nrg.testing.xnat.processing.files.comparators.imaging.PixelFactory;
import org.nrg.testing.xnat.processing.files.comparators.imaging.metrics.DiscreteMetric;
import org.nrg.testing.xnat.processing.files.comparators.imaging.metrics.HammingMetric;
import org.nrg.testing.xnat.processing.files.comparators.imaging.metrics.Metric;
import org.testng.annotations.Test;

public class ComparisonPixelTest {

    private static final ComparisonPixel COLOR_PIXEL1 = PixelFactory.getPixel(3, 6, 9, 0, 0, 0);
    private static final ComparisonPixel COLOR_PIXEL2 = PixelFactory.getPixel(4, -9, -2, 3, -9, -20);
    private static final ComparisonPixel GRAY_PIXEL1 = PixelFactory.getPixel(100, 0);
    private static final ComparisonPixel GRAY_PIXEL2 = PixelFactory.getPixel(99, 100);
    private static final ComparisonPixel GRAY_ZERO_PIXEL = PixelFactory.getPixel(77, 77);
    private static final ComparisonPixel GRAY_ZERO_DIFF_PIXEL = PixelFactory.getPixel(100, 100, true);
    private static final ComparisonPixel COLOR_ZERO_PIXEL = PixelFactory.getPixel(77, 76, 75, 77, 76, 75);
    private static final ComparisonPixel COLOR_ZERO_DIFF_PIXEL = PixelFactory.getPixel(5, 8, 13, 5, 8, 13);
    private static final Metric TAXICAB_DIST = new PNormDistance(1);
    private static final Metric EUCLIDEAN_DIST = new PNormDistance(2);
    private static final Metric DISCRETE_METRIC = new DiscreteMetric();
    private static final Metric HAMMING_METRIC = new HammingMetric();

    @Test
    public void test1Norm() {
        UnitTestUtils.assertDoubleEqual(COLOR_PIXEL1.calculateDistance(TAXICAB_DIST), 18);
        UnitTestUtils.assertDoubleEqual(COLOR_PIXEL2.calculateDistance(TAXICAB_DIST), 19);
        UnitTestUtils.assertDoubleEqual(GRAY_PIXEL1.calculateDistance(TAXICAB_DIST), 100);
        UnitTestUtils.assertDoubleEqual(GRAY_PIXEL2.calculateDistance(TAXICAB_DIST), 1);
        UnitTestUtils.assertDoubleEqual(GRAY_ZERO_PIXEL.calculateDistance(TAXICAB_DIST), 0);
        UnitTestUtils.assertDoubleEqual(GRAY_ZERO_DIFF_PIXEL.calculateDistance(TAXICAB_DIST), 0);
        UnitTestUtils.assertDoubleEqual(COLOR_ZERO_PIXEL.calculateDistance(TAXICAB_DIST), 0);
        UnitTestUtils.assertDoubleEqual(COLOR_ZERO_DIFF_PIXEL.calculateDistance(TAXICAB_DIST), 0);
    }

    @Test
    public void test2Norm() {
        UnitTestUtils.assertDoubleEqual(COLOR_PIXEL1.calculateDistance(EUCLIDEAN_DIST), 3*Math.sqrt(14));
        UnitTestUtils.assertDoubleEqual(COLOR_PIXEL2.calculateDistance(EUCLIDEAN_DIST), 5*Math.sqrt(13));
        UnitTestUtils.assertDoubleEqual(GRAY_PIXEL1.calculateDistance(EUCLIDEAN_DIST), 100);
        UnitTestUtils.assertDoubleEqual(GRAY_PIXEL2.calculateDistance(EUCLIDEAN_DIST), 1);
        UnitTestUtils.assertDoubleEqual(GRAY_ZERO_PIXEL.calculateDistance(EUCLIDEAN_DIST), 0);
        UnitTestUtils.assertDoubleEqual(GRAY_ZERO_DIFF_PIXEL.calculateDistance(EUCLIDEAN_DIST), 0);
        UnitTestUtils.assertDoubleEqual(COLOR_ZERO_PIXEL.calculateDistance(EUCLIDEAN_DIST), 0);
        UnitTestUtils.assertDoubleEqual(COLOR_ZERO_DIFF_PIXEL.calculateDistance(EUCLIDEAN_DIST), 0);
    }

    @Test
    public void testDiscreteMetric() {
        UnitTestUtils.assertDoubleEqual(COLOR_PIXEL1.calculateDistance(DISCRETE_METRIC), 1);
        UnitTestUtils.assertDoubleEqual(COLOR_PIXEL2.calculateDistance(DISCRETE_METRIC), 1);
        UnitTestUtils.assertDoubleEqual(GRAY_PIXEL1.calculateDistance(DISCRETE_METRIC), 1);
        UnitTestUtils.assertDoubleEqual(GRAY_PIXEL2.calculateDistance(DISCRETE_METRIC), 1);
        UnitTestUtils.assertDoubleEqual(GRAY_ZERO_PIXEL.calculateDistance(DISCRETE_METRIC), 0);
        UnitTestUtils.assertDoubleEqual(GRAY_ZERO_DIFF_PIXEL.calculateDistance(DISCRETE_METRIC), 0);
        UnitTestUtils.assertDoubleEqual(COLOR_ZERO_PIXEL.calculateDistance(DISCRETE_METRIC), 0);
        UnitTestUtils.assertDoubleEqual(COLOR_ZERO_DIFF_PIXEL.calculateDistance(DISCRETE_METRIC), 0);
    }
    
    @Test
    public void testHammingMetric() {
        UnitTestUtils.assertDoubleEqual(COLOR_PIXEL1.calculateDistance(HAMMING_METRIC), 3);
        UnitTestUtils.assertDoubleEqual(COLOR_PIXEL2.calculateDistance(HAMMING_METRIC), 2);
        UnitTestUtils.assertDoubleEqual(GRAY_PIXEL1.calculateDistance(HAMMING_METRIC), 1);
        UnitTestUtils.assertDoubleEqual(GRAY_PIXEL2.calculateDistance(HAMMING_METRIC), 1);
        UnitTestUtils.assertDoubleEqual(GRAY_ZERO_PIXEL.calculateDistance(HAMMING_METRIC), 0);
        UnitTestUtils.assertDoubleEqual(GRAY_ZERO_DIFF_PIXEL.calculateDistance(HAMMING_METRIC), 0);
        UnitTestUtils.assertDoubleEqual(COLOR_ZERO_PIXEL.calculateDistance(HAMMING_METRIC), 0);
        UnitTestUtils.assertDoubleEqual(COLOR_ZERO_DIFF_PIXEL.calculateDistance(HAMMING_METRIC), 0);
    }
}