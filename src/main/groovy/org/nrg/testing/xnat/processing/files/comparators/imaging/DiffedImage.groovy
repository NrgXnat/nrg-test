package org.nrg.testing.xnat.processing.files.comparators.imaging

import com.google.common.graph.Graph
import com.google.common.graph.GraphBuilder
import com.google.common.graph.MutableGraph
import groovy.util.logging.Log4j
import ij.ImagePlus
import org.nrg.testing.CollectionUtils
import org.nrg.testing.enums.ImageType
import org.nrg.testing.xnat.processing.exceptions.ImageProcessingException
import org.nrg.testing.xnat.processing.files.comparators.imaging.metrics.Metric
import org.nrg.testing.xnat.processing.files.comparators.imaging.metrics.Metrics
import org.nrg.xnat.util.GraphUtils

@Log4j
class DiffedImage {

    boolean isColor
    protected ComparisonPixel[][][] signedComparisonPixels // z, x, y so we can iterate over "pages"/"slices" (z)
    protected ImageType type

    DiffedImage(File originalImageFile, File generatedImageFile, ImageType type) throws ImageProcessingException {
        this.type = type
        final ImagePlus original = openImage(originalImageFile)
        final ImagePlus generated = openImage(generatedImageFile)
        final List<Integer> originalDimensions = original.dimensions as List<Integer>
        final List<Integer> generatedDimensions = generated.dimensions as List<Integer>
        if (originalDimensions != generatedDimensions) {
            throw new ImageProcessingException("Original image dimensions of ${originalDimensions} did not match generated image dimensions of ${generatedDimensions} in file ${generatedImageFile.name}.")
        }
        if (original.type != generated.type) {
            throw new ImageProcessingException("Original and generated images do not have the same color type for file: ${generatedImageFile.name}")
        }
        if (generated.type in [ImagePlus.GRAY8, ImagePlus.GRAY16, ImagePlus.GRAY32]) {
            isColor = false
        } else if (generated.type in [ImagePlus.COLOR_256, ImagePlus.COLOR_RGB]) {
            isColor = true
        } else {
            throw new ImageProcessingException("Unknown color type for file: ${generatedImageFile.name}")
        }
        readComparisonPixels(original, generated)
        original.close()
        generated.close()
    }

    DiffedImage(File originalImageFile, File generatedImageFile) throws ImageProcessingException {
        this(originalImageFile, generatedImageFile, null)
    }

    DiffedImage(String originalImage, String generatedImage) throws ImageProcessingException {
        this(new File(originalImage), new File(generatedImage))
    }

    List<ComparisonPixel> getNonzeroPixels() {
        (0 ..< getPages()).collect { z ->
            getNonzeroPixels(z)
        }.flatten() as List<ComparisonPixel>
    }

    List<ComparisonPixel> getNonzeroPixels(int z) {
        final List<ComparisonPixel> pixels = []
        (0 ..< width).each { x ->
            (0 ..< height).each { y ->
                final ComparisonPixel pixel = signedComparisonPixels[z][x][y]
                if (!(pixel instanceof ZeroComparisonPixel) && !(pixel instanceof ZeroDiffPixel)) {
                    pixel.setCoordinates(z, x, y)
                    pixels << pixel
                }
            }
        }
        pixels
    }

    int getPages() {
        signedComparisonPixels.length
    }

    int getWidth() {
        signedComparisonPixels[0].length
    }

    int getHeight() {
        signedComparisonPixels[0][0].length
    }

    int getTotalNumPixels() {
        pages * width * height
    }

    ComparisonPixel getPixel(int slice, int x, int y) {
        signedComparisonPixels[slice][x][y]
    }

    ComparisonPixel getPixel(int x, int y) {
        getPixel(0, x, y)
    }

    int getAbsoluteDeviation() {
        Math.round(getTotalStackDifference(Metrics.TAXICAB)) as int
    }

    double getSquaredDeviation() {
        getTotalStackDifference(Metrics.EUCLIDEAN)
    }

    int getNumNonzeroPixels() {
        Math.round(getTotalStackDifference(Metrics.DISCRETE)) as int
    }

    double getPercentNonzeroPixels() {
        100 * numNonzeroPixels / totalNumPixels
    }

    double getTotalStackDifference(Metric metric) {
        (0 ..< pages).sum { int z ->
            (0 ..< width).sum { int x ->
                (0 ..< height).sum { int y ->
                    signedComparisonPixels[z][x][y].calculateDistance(metric)
                }
            }
        } as double
    }

    Graph<ComparisonPixel> nonzeroPixelAdjacencyGraph(int sliceNum) {
        final List<ComparisonPixel> nonzeroPixels = getNonzeroPixels(sliceNum)
        final MutableGraph<ComparisonPixel> adjacencyGraph = GraphBuilder.undirected().build()

        nonzeroPixels.each { pixel ->
            adjacencyGraph.addNode(pixel)
        }

        CollectionUtils.subsetsOfSize2(nonzeroPixels).each { pair ->
            final ComparisonPixel pixel1 = pair.first
            final ComparisonPixel pixel2 = pair.second
            if (pixel1.isAdjacent(pixel2)) {
                adjacencyGraph.putEdge(pixel1, pixel2)
            }
        }

        adjacencyGraph
    }

    Set<Set<ComparisonPixel>> getConnectedComponents(int sliceNum) {
        GraphUtils.findConnectedComponents(nonzeroPixelAdjacencyGraph(sliceNum))
    }

    int getMaximalConnectedComponent(int sliceNum) {
        GraphUtils.findMaximalConnectedComponent(nonzeroPixelAdjacencyGraph(sliceNum))
    }

    int getMaximalConnectedComponent() {
        (0 ..< pages).collect { z ->
            getMaximalConnectedComponent(z)
        }.max()
    }

    private ImagePlus openImage(File image) throws ImageProcessingException {
        if (type == null) {
            final String imageName = image.name.toLowerCase()
            if (imageName.endsWith('.nii')) {
                type = ImageType.NIFTI
            } else if (imageName.endsWith('.dcm') || imageName.endsWith('.ima')) {
                type = ImageType.DICOM
            } else {
                type = ImageType.PLAIN_IMAGE
            }
        }
        try {
            final ImagePlus read = type.readImage(image)
            if (read != null) {
                return read
            } else {
                throw new UnsupportedOperationException('unknown reason.')
            }
        } catch (Exception e) {
            log.warn('Failed to process image', e)
            throw new ImageProcessingException("Could not open image: ${image.name} due to: ${e.message}")
        }
    }

    private void readComparisonPixels(ImagePlus original, ImagePlus generated) {
        final int numSlices = generated.NSlices
        final int width = generated.width
        final int height = generated.height
        signedComparisonPixels = new ComparisonPixel[numSlices][width][height]
        log.debug("Attempting to diff image with ${numSlices} slice(s) and 2-D resolution ${width} by ${height}...")

        (0 ..< numSlices).each { z ->
            original.setSliceWithoutUpdate(z + 1)
            generated.setSliceWithoutUpdate(z + 1)
            (0 ..< width).each { x ->
                (0 ..< height).each { y ->
                    signedComparisonPixels[z][x][y] = readComparisonPixel(x, y, original.getPixel(x, y), generated.getPixel(x, y))
                }
            }
        }
    }

    // x and y are pointless here, but used in xnat_rest_tests to override this method
    protected ComparisonPixel readComparisonPixel(int x, int y, int[] original, int[] generated) {
        isColor ? PixelFactory.getPixel(original[0], original[1], original[2], generated[0], generated[1], generated[2]) : PixelFactory.getPixel(original[0], generated[0])
    }

}
