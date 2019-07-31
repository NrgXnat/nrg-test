package org.nrg.testing

import org.nrg.testing.xnat.processing.files.comparators.imaging.DiffedImage

import java.nio.file.Paths

class BaseTestWithDiffedImages {

    protected static File colorImageOriginal = resolveImage('test_color.gif')
    protected static File colorImageGenerated = resolveImage('test_color_greened.gif')
    protected static File grayImageOriginal = resolveImage('gray_lou.gif')
    protected static File grayImageGenerated = resolveImage('gray_lou_edited.gif')
    protected static File niftiOriginal = resolveImage('avg152T1_LR_nifti.nii')
    protected static File niftiGenerated = resolveImage('avg152T1_RL_nifti.nii')
    protected static File niftiDifferentDimensions = resolveImage('zstat1.nii')
    protected static File pixelGraphOriginal = resolveImage('pixel_graph_test.png')
    protected static File pixelGraphGenerated = resolveImage('pixel_graph_test_edited.png')
    protected static File niftiCheckeredStack = resolveImage('checkered_nifti.nii')
    protected static File niftiBlackBoxStack = resolveImage('nifti_black_box_stack.nii')
    protected static final DiffedImage colorDiffImage = getImageDiff(colorImageOriginal, colorImageGenerated) // should contain 15 nonzero pixels: 5 (80, 230, 160) pixels (purple - green) and 10 (220, 35, 0) pixels (yellow - green)
    protected static final DiffedImage grayDiffImage = getImageDiff(grayImageOriginal, grayImageGenerated) // should contain only 2 nonzero pixels: (180) and (254)
    protected static final DiffedImage zeroNiftiImage = getImageDiff(niftiOriginal, niftiOriginal)
    protected static final DiffedImage differentNifti = getImageDiff(niftiOriginal, niftiGenerated)
    protected static final DiffedImage colorGraphImage = getImageDiff(pixelGraphOriginal, pixelGraphGenerated)
    protected static final DiffedImage checkeredNifti = getImageDiff(niftiCheckeredStack, niftiBlackBoxStack)

    protected static DiffedImage getImageDiff(File original, File generated) {
        new DiffedImage(original, generated)
    }

    protected static File resolveImage(String imageName) {
        Paths.get(UnitTestUtils.DATA_LOCATION, imageName).toFile()
    }

}
