package org.nrg.testing.tests

import org.nrg.testing.BaseTestWithDiffedImages
import org.nrg.testing.FileIOUtils
import org.nrg.testing.UnitTestUtils
import org.nrg.testing.xnat.processing.exceptions.FileValidationException
import org.nrg.testing.xnat.processing.exceptions.ImageProcessingException
import org.nrg.testing.xnat.processing.files.comparators.FileComparator
import org.nrg.testing.xnat.processing.files.comparators.FileSizeComparator
import org.nrg.testing.xnat.processing.files.comparators.MD5_Comparator
import org.nrg.testing.xnat.processing.files.comparators.TextComparator
import org.nrg.testing.xnat.processing.files.comparators.imaging.DiffedImage
import org.nrg.testing.xnat.processing.files.comparators.imaging.ImageComparator
import org.nrg.testing.xnat.processing.files.comparators.imaging.ImageDeviationComparator
import org.nrg.testing.xnat.processing.files.comparators.imaging.NumberPixelsComparator
import org.nrg.testing.xnat.processing.files.comparators.imaging.PercentPixelsComparator
import org.nrg.testing.xnat.processing.files.comparators.imaging.PixelClusterComparator
import org.nrg.testing.xnat.processing.files.resources.ProcessingResourceFile
import org.testng.annotations.Test

import java.nio.file.Path
import java.nio.file.Paths

import static org.testng.AssertJUnit.*

class FileComparatorTest extends BaseTestWithDiffedImages {

    private static final Path dataPath = Paths.get(UnitTestUtils.DATA_LOCATION)
    private static final File dataDirectory = dataPath.toFile()
    private static final String MD5_GRAY_ORIGINAL = 'dc0fe4c8b8693ad0a1e7ec22b0de537a'

    @Test
    void testGeneralImageComparatorSatisfied() {
        final ImageDeviationComparator imageDeviationComparator = new ImageDeviationComparator(maxGrayscaleDeviation : 434, maxColorscaleDeviation : 4900)
        imageDeviationComparator.checkFileMatches(dataDirectory, colorImageGenerated, new ProcessingResourceFile(compareTo : colorImageOriginal.name))
        imageDeviationComparator.checkFileMatches(dataDirectory, grayImageGenerated, new ProcessingResourceFile(compareTo : grayImageGenerated.name))
    }

    @Test
    void testGeneralImageComparatorUnsatisfied() {
        final ImageDeviationComparator imageDeviationComparator = new ImageDeviationComparator(maxGrayscaleDeviation : 433, maxColorscaleDeviation : 4899)
        verifyFileMatchCheckExceptionThrown(imageDeviationComparator, colorImageGenerated, new ProcessingResourceFile(compareTo : 'missing.gif'), 'Could not find original file: missing.gif')
        verifyFileMatchCheckExceptionThrown(
                imageDeviationComparator,
                niftiDifferentDimensions,
                new ProcessingResourceFile(compareTo : niftiOriginal.name),
                'Generated image file zstat1.nii differs too much from expected file (avg152T1_LR_nifti.nii): Original image dimensions of [91, 109, 1, 91, 1] did not match generated image dimensions of [64, 64, 1, 21, 1] in file zstat1.nii.'
        )
        verifyFileMatchCheckExceptionThrown(
                imageDeviationComparator,
                colorImageGenerated,
                new ProcessingResourceFile(compareTo : colorImageOriginal.name),
                'Generated image file test_color_greened.gif differs too much from expected file (test_color.gif): 1-norm color deviation of 4900 exceeds maximum allowed of 4899'
        )
    }

    @Test
    void testImageDeviationComparatorSatisfied() {
        final ImageDeviationComparator imageDeviationComparator = new ImageDeviationComparator(maxGrayscaleDeviation : 434, maxColorscaleDeviation : 4900)
        imageDeviationComparator.checkDiffedImage(colorDiffImage)
        imageDeviationComparator.checkDiffedImage(grayDiffImage)
        new ImageDeviationComparator(maxGrayscaleDeviation : 2295).checkDiffedImage(checkeredNifti)
    }

    @Test
    void testImageDeviationComparatorUnsatisfied() {
        final ImageDeviationComparator imageDeviationComparator = new ImageDeviationComparator(maxGrayscaleDeviation : 433, maxColorscaleDeviation : 4899)
        verifyDiffedCheckExceptionThrown(imageDeviationComparator, colorDiffImage, '1-norm color deviation of 4900 exceeds maximum allowed of 4899')
        verifyDiffedCheckExceptionThrown(imageDeviationComparator, grayDiffImage, '1-norm grayscale deviation of 434 exceeds maximum allowed of 433')
        verifyDiffedCheckExceptionThrown(new ImageDeviationComparator(maxGrayscaleDeviation : 2294), checkeredNifti, '1-norm grayscale deviation of 2295 exceeds maximum allowed of 2294')
    }

    @Test
    void testNumPixelsComparatorSatisfied() {
        new NumberPixelsComparator(maxDifferingPixels : 15).checkDiffedImage(colorDiffImage)
        new NumberPixelsComparator(maxDifferingPixels :  2).checkDiffedImage(grayDiffImage)
        new NumberPixelsComparator(maxDifferingPixels :  9).checkDiffedImage(checkeredNifti)
    }

    @Test
    void testNumPixelsComparatorUnsatisfied() {
        verifyDiffedCheckExceptionThrown(new NumberPixelsComparator(maxDifferingPixels : 14), colorDiffImage, '15 pixels differed, more than the maximum allowed of 14')
        verifyDiffedCheckExceptionThrown(new NumberPixelsComparator(maxDifferingPixels :  1), grayDiffImage, '2 pixels differed, more than the maximum allowed of 1')
        verifyDiffedCheckExceptionThrown(new NumberPixelsComparator(maxDifferingPixels :  8), checkeredNifti, '9 pixels differed, more than the maximum allowed of 8')
    }

    @Test
    void testPercentPixelsComparatorSatisfied() {
        new PercentPixelsComparator(maxPercentError : 0.38).checkDiffedImage(colorDiffImage)
        new PercentPixelsComparator(maxPercentError : 0.0055).checkDiffedImage(grayDiffImage)
        new PercentPixelsComparator(maxPercentError : 50.01).checkDiffedImage(checkeredNifti)
    }

    @Test
    void testPercentPixelsComparatorUnsatisfied() {
        verifyDiffedCheckExceptionThrown(new PercentPixelsComparator(maxPercentError : 0.37), colorDiffImage, '0.375% of pixels differed, more than the maximum allowed of 0.37%')
        verifyDiffedCheckExceptionThrown(new PercentPixelsComparator(maxPercentError : 0.005), grayDiffImage, '0.0053% of pixels differed, more than the maximum allowed of 0.005%')
        verifyDiffedCheckExceptionThrown(new PercentPixelsComparator(maxPercentError : 49.99), checkeredNifti, '50.0% of pixels differed, more than the maximum allowed of 49.99%')
    }

    @Test
    void testClusterComparatorSatisfied() {
        new PixelClusterComparator(maxClusterSize : 11).checkDiffedImage(colorGraphImage)
        new PixelClusterComparator(maxClusterSize :  5).checkDiffedImage(checkeredNifti)
    }

    @Test
    void testClusterComparatorUnsatisfied() {
        verifyDiffedCheckExceptionThrown(new PixelClusterComparator(maxClusterSize : 10), colorGraphImage, 'Cluster of 11 differing pixels located, larger than the maximum allowed of 10')
        verifyDiffedCheckExceptionThrown(new PixelClusterComparator(maxClusterSize :  4), checkeredNifti, 'Cluster of 5 differing pixels located, larger than the maximum allowed of 4')
    }

    @Test
    void testFileSizeComparatorSatisfied() {
        new FileSizeComparator(tolerance :   0).checkFileMatches(dataDirectory, niftiCheckeredStack, new ProcessingResourceFile(expectedSize : 370))
        new FileSizeComparator(tolerance : 2.8).checkFileMatches(dataDirectory, niftiCheckeredStack, new ProcessingResourceFile(expectedSize : 360))
    }

    @Test
    void testFileSizeComparatorUnsatisfied() {
        verifyFileMatchCheckExceptionThrown(
                new FileSizeComparator(tolerance : 0),
                niftiCheckeredStack,
                new ProcessingResourceFile(expectedSize : 371),
                'The file size for the file checkered_nifti.nii had a value of 370, which did not match the expected value of 371 to within 0.0 percent error.'
        )
        verifyFileMatchCheckExceptionThrown(
                new FileSizeComparator(tolerance : 2.7),
                niftiCheckeredStack,
                new ProcessingResourceFile(expectedSize : 360),
                'The file size for the file checkered_nifti.nii had a value of 370, which did not match the expected value of 360 to within 2.7 percent error.'
        )
    }

    @Test
    void testMd5ComparatorSatisfied() {
        new MD5_Comparator().checkFileMatches(dataDirectory, grayImageOriginal, new ProcessingResourceFile(md5 : MD5_GRAY_ORIGINAL))
    }

    @Test
    void testMd5ComparatorUnsatisfied() {
        verifyFileMatchCheckExceptionThrown(new MD5_Comparator(), grayImageGenerated, new ProcessingResourceFile(md5 : MD5_GRAY_ORIGINAL), 'MD5 checksum did not match expected value for file: gray_lou_edited.gif')
    }

    @Test
    void testTextComparatorSatisfied() {
        new TextComparator().checkFileMatches(dataDirectory, FileIOUtils.loadResource('expected_text.txt'), new ProcessingResourceFile(expectedText : 'Voxels: [(0,1,0),(893,984,54)]'))
    }

    @Test
    void testTextComparatorUnsatisfied() {
        verifyFileMatchCheckExceptionThrown(
                new TextComparator(),
                FileIOUtils.loadResource('expected_text.txt'),
                new ProcessingResourceFile(expectedText : 'Voxels: [(0,1,0),(892,983,51)]'),
                'Expected text of Voxels: [(0,1,0),(892,983,51)] for file expected_text.txt did not match actual text: Voxels: [(0,1,0),(893,984,54)].'
        )
    }

    private void verifyFileMatchCheckExceptionThrown(FileComparator fileComparator, File generatedFile, ProcessingResourceFile processingResourceFile, String expectedMessage) {
        try {
            fileComparator.checkFileMatches(dataDirectory, generatedFile, processingResourceFile)
            failExceptionMissing(fileComparator)
        } catch (FileValidationException fve) {
            assertEquals(expectedMessage, fve.message)
        }
    }

    private void verifyDiffedCheckExceptionThrown(ImageComparator imageComparator, DiffedImage diffedImage, String expectedMessage) {
        try {
            imageComparator.checkDiffedImage(diffedImage)
            failExceptionMissing(imageComparator)
        } catch (ImageProcessingException ipe) {
            assertEquals(expectedMessage, ipe.message)
        }
    }

    private void failExceptionMissing(FileComparator fileComparator) {
        fail("${fileComparator.class.simpleName} should have thrown an exception.")
    }

}
