package org.nrg.testing.tests

import org.nrg.testing.UnitTestUtils
import org.nrg.testing.xnat.processing.exceptions.ImageProcessingException
import org.nrg.testing.xnat.processing.files.comparators.imaging.ComparisonPixel
import org.nrg.testing.xnat.processing.files.comparators.imaging.DiffedImage
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

import static org.testng.AssertJUnit.*

import java.nio.file.Paths

class DiffedImageTest {

    private static DiffedImage colorDiffImage, grayDiffImage, zeroNiftiImage, differentNifti, colorGraphImage

    @BeforeClass
    void readImages() {
        try {
            // colorDiffImage should contain 15 nonzero pixels: 5 (80, 230, 160) pixels (purple - green) and 10 (220, 35, 0) pixels (yellow - green)
            colorDiffImage = getImageDiff('test_color.gif', 'test_color_greened.gif')
            // grayDiffImage should contain only 2 nonzero pixels: (180) and (254)
            grayDiffImage = getImageDiff('gray_lou.gif', 'gray_lou_edited.gif')
            // zeroNiftiImage should contain only zero pixels
            zeroNiftiImage = getImageDiff('avg152T1_LR_nifti.nii', 'avg152T1_LR_nifti.nii')
            differentNifti = getImageDiff('avg152T1_LR_nifti.nii', 'avg152T1_RL_nifti.nii')
            colorGraphImage = getImageDiff('pixel_graph_test.png', 'pixel_graph_test_edited.png')
        } catch (ImageProcessingException ipe) {
            fail("Couldn't read images:\n${ipe}")
        }
    }

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
        UnitTestUtils.assertDoubleEqual(50*Math.sqrt(849) + 50*Math.sqrt(1985), colorDiffImage.squaredDeviation)
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
        UnitTestUtils.assertDoubleEqual(100 * 15/(80 * 50), colorDiffImage.percentNonzeroPixels)
    }

    @Test
    void testPixelPercentErrorWithGrayscale() {
        UnitTestUtils.assertDoubleEqual(100 * 2/(250 * 150), grayDiffImage.percentNonzeroPixels)
    }

    @Test
    void testZeroNiftiImage() {
        assertEquals(0, zeroNiftiImage.absoluteDeviation)
        UnitTestUtils.assertDoubleEqual(0, zeroNiftiImage.squaredDeviation)
        assertEquals(0, zeroNiftiImage.numNonzeroPixels)
        assertEquals(0, zeroNiftiImage.nonzeroPixels.size())
    }

    @Test
    void testNiftiImageComparison() {
        assertTrue(differentNifti.absoluteDeviation > 0)
        assertTrue(differentNifti.squaredDeviation  > 0)
        assertTrue(differentNifti.numNonzeroPixels  > 0)
        assertTrue(differentNifti.pages == 91)
    }

    @Test()
    void testNiftiDimensionalMismatch() {
        try {
            getImageDiff('avg152T1_LR_nifti.nii', 'zstat1.nii')
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

    private DiffedImage getImageDiff(String original, String generated) throws ImageProcessingException {
        new DiffedImage(Paths.get(UnitTestUtils.DATA_LOCATION, original).toFile(), Paths.get(UnitTestUtils.DATA_LOCATION, generated).toFile())
    }
    
}
