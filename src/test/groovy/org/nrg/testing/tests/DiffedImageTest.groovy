package org.nrg.testing.tests

import org.nrg.testing.BaseTestWithDiffedImages
import org.nrg.testing.UnitTestUtils
import org.nrg.testing.xnat.processing.exceptions.ImageProcessingException
import org.nrg.testing.xnat.processing.files.comparators.imaging.ComparisonPixel
import org.testng.annotations.Test

import static org.testng.AssertJUnit.*

class DiffedImageTest extends BaseTestWithDiffedImages {

    @Test
    void testAbsoluteDeviationWithColor() {
        assertEquals(5*470 + 10*255, colorDiffImage.absoluteDeviation)
    }

    @Test
    void testAbsoluteDeviationWithGrayscale() {
        assertEquals(180 + 254, grayDiffImage.absoluteDeviation)
    }

    @Test
    void testSquareDeviationWithColor() {
        UnitTestUtils.assertDoubleEqual(50 * Math.sqrt(849) + 50 * Math.sqrt(1985), colorDiffImage.squaredDeviation)
    }

    @Test
    void testSquareDeviationWithGrayscale() {
        UnitTestUtils.assertDoubleEqual(180 + 254, grayDiffImage.squaredDeviation)
    }

    @Test
    void testDiscreteDeviationWithColor() {
        assertEquals(15, colorDiffImage.numNonzeroPixels)
        assertEquals(15, colorDiffImage.nonzeroPixels.size())
    }

    @Test
    void testDiscreteDeviationWithGrayscale() {
        assertEquals(2, grayDiffImage.numNonzeroPixels)
        assertEquals(2, grayDiffImage.nonzeroPixels.size())
    }

    @Test
    void testPixelPercentErrorWithColor() {
        UnitTestUtils.assertDoubleEqual(100 * 15 / (80 * 50), colorDiffImage.percentNonzeroPixels)
    }

    @Test
    void testPixelPercentErrorWithGrayscale() {
        UnitTestUtils.assertDoubleEqual(100 * 2 / (250 * 150), grayDiffImage.percentNonzeroPixels)
    }

    @Test
    void testZeroNiftiImage() {
        assertEquals(0, zeroNiftiImage.absoluteDeviation)
        UnitTestUtils.assertDoubleEqual(0, zeroNiftiImage.squaredDeviation)
        assertEquals(0, zeroNiftiImage.numNonzeroPixels)
        assertEquals(0, zeroNiftiImage.nonzeroPixels.size())
    }

    @Test
    void testBaseNiftiImageComparison() {
        assertTrue(differentNifti.absoluteDeviation > 0)
        assertTrue(differentNifti.squaredDeviation  > 0)
        assertTrue(differentNifti.numNonzeroPixels  > 0)
        assertTrue(differentNifti.pages == 91)
    }

    @Test
    void testNiftiSquareDeviation() {
        UnitTestUtils.assertDoubleEqual(9 * 255, checkeredNifti.squaredDeviation)
    }

    @Test
    void testNiftiAbsoluteDeviation() {
        assertEquals(9 * 255, checkeredNifti.absoluteDeviation)
    }

    @Test
    void testNiftiDiscreteDeviation() {
        assertEquals(9, checkeredNifti.numNonzeroPixels)
        assertEquals(9, checkeredNifti.nonzeroPixels.size())
    }

    @Test
    void testNiftiPercentError() {
        UnitTestUtils.assertDoubleEqual(50, checkeredNifti.percentNonzeroPixels)
    }

    @Test()
    void testNiftiDimensionalMismatch() {
        try {
            getImageDiff(niftiOriginal, niftiDifferentDimensions)
            fail('Diffing two Nifti images with different dimensions threw no exception.')
        } catch (ImageProcessingException e) {
            assertTrue('Exception produced by diffing different dimensional Nifti was not the right one.', e.getMessage().contains('image dimensions'))
        }
    }

    @Test
    void testImageComponentClustering() {
        final ComparisonPixel pixel1 = colorGraphImage.getPixel(0, 0)
        final ComparisonPixel pixel2 = colorGraphImage.getPixel(1, 0)
        final ComparisonPixel pixel3 = colorGraphImage.getPixel(2, 0)
        final ComparisonPixel pixel4 = colorGraphImage.getPixel(3, 0)
        final ComparisonPixel pixel5 = colorGraphImage.getPixel(4, 0)
        final ComparisonPixel pixel6 = colorGraphImage.getPixel(49, 0)
        final ComparisonPixel pixel7 = colorGraphImage.getPixel(21, 23)
        final ComparisonPixel pixel8 = colorGraphImage.getPixel(22, 23)
        final ComparisonPixel pixel9 = colorGraphImage.getPixel(23, 23)
        final ComparisonPixel pixel10 = colorGraphImage.getPixel(23, 24)
        final ComparisonPixel pixel11 = colorGraphImage.getPixel(23, 25)
        final ComparisonPixel pixel12 = colorGraphImage.getPixel(24, 25)
        final ComparisonPixel pixel13 = colorGraphImage.getPixel(25, 25)
        final ComparisonPixel pixel14 = colorGraphImage.getPixel(26, 25)
        final ComparisonPixel pixel15 = colorGraphImage.getPixel(23, 26)
        final ComparisonPixel pixel16 = colorGraphImage.getPixel(22, 26)
        final ComparisonPixel pixel17 = colorGraphImage.getPixel(22, 27)
        final ComparisonPixel pixel18 = colorGraphImage.getPixel(27, 26)
        final ComparisonPixel pixel19 = colorGraphImage.getPixel(27, 27)
        final ComparisonPixel pixel20 = colorGraphImage.getPixel(28, 27)
        final ComparisonPixel pixel21 = colorGraphImage.getPixel(2, 39)
        final ComparisonPixel pixel22 = colorGraphImage.getPixel(3, 39)
        final ComparisonPixel pixel23 = colorGraphImage.getPixel(4, 39)
        final ComparisonPixel pixel24 = colorGraphImage.getPixel(2, 40)
        final ComparisonPixel pixel25 = colorGraphImage.getPixel(3, 40)
        final ComparisonPixel pixel26 = colorGraphImage.getPixel(4, 40)
        final ComparisonPixel pixel27 = colorGraphImage.getPixel(2, 41)
        final ComparisonPixel pixel28 = colorGraphImage.getPixel(3, 41)
        final ComparisonPixel pixel29 = colorGraphImage.getPixel(4, 41)

        final Set<Set<ComparisonPixel>> connectedComponents = colorGraphImage.getConnectedComponents(0)
        final Set<ComparisonPixel> expectedCC1 = [pixel1, pixel2, pixel3, pixel4, pixel5] as Set
        final Set<ComparisonPixel> expectedCC2 = [pixel6] as Set
        final Set<ComparisonPixel> expectedCC3 = [pixel7, pixel8, pixel9, pixel10, pixel11, pixel12, pixel13, pixel14, pixel15, pixel16, pixel17] as Set
        final Set<ComparisonPixel> expectedCC4 = [pixel18, pixel19, pixel20] as Set
        final Set<ComparisonPixel> expectedCC5 = [pixel21, pixel22, pixel23, pixel24, pixel25, pixel26, pixel27, pixel28, pixel29] as Set

        assertEquals([expectedCC1, expectedCC2, expectedCC3, expectedCC4, expectedCC5] as Set, connectedComponents)
        assertEquals(expectedCC3.size(), colorGraphImage.maximalConnectedComponent)
    }
    
}
