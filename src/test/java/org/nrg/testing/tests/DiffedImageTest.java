package org.nrg.testing.tests;

import com.google.common.collect.Sets;
import org.nrg.testing.CommonUtils;
import org.nrg.testing.UnitTestUtils;
import org.nrg.testing.xnat.processing.files.ImageProcessingException;
import org.nrg.testing.xnat.processing.files.comparators.imaging.ComparisonPixel;
import org.nrg.testing.xnat.processing.files.comparators.imaging.DiffedImage;
import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Paths;
import java.util.Set;

public class DiffedImageTest {

    private static DiffedImage colorDiffImage, grayDiffImage, zeroNiftiImage, differentNifti, colorGraphImage;

    @BeforeClass
    public void readImages() {
        try {
            // colorDiffImage should contain 15 nonzero pixels: 5 (80, 230, 160) pixels (purple - green) and 10 (220, 35, 0) pixels (yellow - green)
            colorDiffImage = getImageDiff("test_color.gif", "test_color_greened.gif");
            // grayDiffImage should contain only 2 nonzero pixels: (180) and (254)
            grayDiffImage = getImageDiff("gray_lou.gif", "gray_lou_edited.gif");
            // zeroNiftiImage should contain only zero pixels
            zeroNiftiImage = getImageDiff("avg152T1_LR_nifti.nii", "avg152T1_LR_nifti.nii");
            differentNifti = getImageDiff("avg152T1_LR_nifti.nii", "avg152T1_RL_nifti.nii");
            colorGraphImage = getImageDiff("pixel_graph_test.png", "pixel_graph_test_edited.png");
        } catch (ImageProcessingException ipe) {
            Assert.fail("Couldn't read images:\n" + ipe);
        }
    }

    @Test
    public void testAbsoluteDeviationWithColor() {
        Assert.assertEquals(colorDiffImage.getAbsoluteDeviation(), 5*470 + 10*255);
    }

    @Test
    public void testAbsoluteDeviationWithGrayscale() {
        Assert.assertEquals(grayDiffImage.getAbsoluteDeviation(), 180 + 254);
    }

    @Test
    public void testSquareDeviationWithColor() {
        UnitTestUtils.assertDoubleEqual(colorDiffImage.getSquaredDeviation(), 50*Math.sqrt(849) + 50*Math.sqrt(1985));
    }

    @Test
    public void testSquareDeviationWithGrayscale() {
        UnitTestUtils.assertDoubleEqual(grayDiffImage.getSquaredDeviation(), 180 + 254);
    }

    @Test
    public void testDiscreteDeviationWithColor() {
        Assert.assertEquals(colorDiffImage.getNumNonzeroPixels(), 15);
        Assert.assertEquals(colorDiffImage.getNonzeroPixels().size(), 15);
    }

    @Test
    public void testDiscreteDeviationWithGrayscale() {
        Assert.assertEquals(grayDiffImage.getNumNonzeroPixels(), 2);
        Assert.assertEquals(grayDiffImage.getNonzeroPixels().size(), 2);
    }

    @Test
    public void testPixelPercentErrorWithColor() {
        UnitTestUtils.assertDoubleEqual(colorDiffImage.getPercentNonzeroPixels(), 100.0*15/(80*50));
    }

    @Test
    public void testPixelPercentErrorWithGrayscale() {
        UnitTestUtils.assertDoubleEqual(grayDiffImage.getPercentNonzeroPixels(), 100.0*2/(250*150));
    }

    @Test
    public void testZeroNiftiImage() {
        Assert.assertEquals(zeroNiftiImage.getAbsoluteDeviation(), 0);
        UnitTestUtils.assertDoubleEqual(zeroNiftiImage.getSquaredDeviation(), 0);
        Assert.assertEquals(zeroNiftiImage.getNumNonzeroPixels(), 0);
        Assert.assertEquals(zeroNiftiImage.getNonzeroPixels().size(), 0);
    }

    @Test
    public void testNiftiImageComparison() {
        Assert.assertTrue(differentNifti.getAbsoluteDeviation() > 0);
        Assert.assertTrue(differentNifti.getSquaredDeviation()  > 0);
        Assert.assertTrue(differentNifti.getNumNonzeroPixels()  > 0);
        Assert.assertTrue(differentNifti.getPages() == 91);
    }

    @Test()
    public void testNiftiDimensionalMismatch() {
        try {
            new DiffedImage(Paths.get(UnitTestUtils.DATA_LOCATION, "avg152T1_LR_nifti.nii").toFile(), Paths.get(UnitTestUtils.DATA_LOCATION, "zstat1.nii").toFile());
            AssertJUnit.fail("Diffing two Nifti images with different dimensions threw no exception.");
        } catch (ImageProcessingException e) {
            AssertJUnit.assertTrue("Exception produced by diffing different dimensional Nifti was not the right one.", e.getMessage().contains("image dimensions"));
        }
    }

    @Test
    public void testImageComponentClustering() {
        final ComparisonPixel pixel1 = colorGraphImage.getPixel(0, 0);
        final ComparisonPixel pixel2 = colorGraphImage.getPixel(1, 0);
        final ComparisonPixel pixel3 = colorGraphImage.getPixel(2, 0);
        final ComparisonPixel pixel4 = colorGraphImage.getPixel(3, 0);
        final ComparisonPixel pixel5 = colorGraphImage.getPixel(4, 0);
        final ComparisonPixel pixel6 = colorGraphImage.getPixel(49, 0);
        final ComparisonPixel pixel7 = colorGraphImage.getPixel(21, 23);
        final ComparisonPixel pixel8 = colorGraphImage.getPixel(22, 23);
        final ComparisonPixel pixel9 = colorGraphImage.getPixel(23, 23);
        final ComparisonPixel pixel10 = colorGraphImage.getPixel(23, 24);
        final ComparisonPixel pixel11 = colorGraphImage.getPixel(23, 25);
        final ComparisonPixel pixel12 = colorGraphImage.getPixel(24, 25);
        final ComparisonPixel pixel13 = colorGraphImage.getPixel(25, 25);
        final ComparisonPixel pixel14 = colorGraphImage.getPixel(26, 25);
        final ComparisonPixel pixel15 = colorGraphImage.getPixel(23, 26);
        final ComparisonPixel pixel16 = colorGraphImage.getPixel(22, 26);
        final ComparisonPixel pixel17 = colorGraphImage.getPixel(22, 27);
        final ComparisonPixel pixel18 = colorGraphImage.getPixel(27, 26);
        final ComparisonPixel pixel19 = colorGraphImage.getPixel(27, 27);
        final ComparisonPixel pixel20 = colorGraphImage.getPixel(28, 27);
        final ComparisonPixel pixel21 = colorGraphImage.getPixel(2, 39);
        final ComparisonPixel pixel22 = colorGraphImage.getPixel(3, 39);
        final ComparisonPixel pixel23 = colorGraphImage.getPixel(4, 39);
        final ComparisonPixel pixel24 = colorGraphImage.getPixel(2, 40);
        final ComparisonPixel pixel25 = colorGraphImage.getPixel(3, 40);
        final ComparisonPixel pixel26 = colorGraphImage.getPixel(4, 40);
        final ComparisonPixel pixel27 = colorGraphImage.getPixel(2, 41);
        final ComparisonPixel pixel28 = colorGraphImage.getPixel(3, 41);
        final ComparisonPixel pixel29 = colorGraphImage.getPixel(4, 41);

        final Set<Set<ComparisonPixel>> connectedComponents = colorGraphImage.getConnectedComponents(0);
        final Set<ComparisonPixel> expectedCC1 = Sets.newHashSet(pixel1, pixel2, pixel3, pixel4, pixel5);
        final Set<ComparisonPixel> expectedCC2 = Sets.newHashSet(pixel6);
        final Set<ComparisonPixel> expectedCC3 = Sets.newHashSet(pixel7, pixel8, pixel9, pixel10, pixel11, pixel12, pixel13, pixel14, pixel15, pixel16, pixel17);
        final Set<ComparisonPixel> expectedCC4 = Sets.newHashSet(pixel18, pixel19, pixel20);
        final Set<ComparisonPixel> expectedCC5 = Sets.newHashSet(pixel21, pixel22, pixel23, pixel24, pixel25, pixel26, pixel27, pixel28, pixel29);

        Assert.assertEquals(connectedComponents, Sets.newHashSet(expectedCC1, expectedCC2, expectedCC3, expectedCC4, expectedCC5));
        Assert.assertEquals(colorGraphImage.getMaximalConnectedComponent(), expectedCC3.size());
    }

    private DiffedImage getImageDiff(String original, String generated) throws ImageProcessingException {
        return new DiffedImage(Paths.get(UnitTestUtils.DATA_LOCATION, original).toFile(), Paths.get(UnitTestUtils.DATA_LOCATION, generated).toFile());
    }
}
