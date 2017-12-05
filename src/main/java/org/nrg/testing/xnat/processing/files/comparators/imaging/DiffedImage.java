package org.nrg.testing.xnat.processing.files.comparators.imaging;

import ij.ImagePlus;
import ij.ImageStack;
import ij.io.Opener;
import loci.formats.in.NiftiReader;
import loci.plugins.util.ImageProcessorReader;
import org.apache.log4j.Logger;
import org.nrg.testing.util.GraphUtils;
import org.nrg.testing.xnat.processing.exceptions.ImageProcessingException;
import org.nrg.testing.xnat.processing.files.comparators.imaging.metrics.DiscreteMetric;
import org.nrg.testing.xnat.processing.files.comparators.imaging.metrics.Metric;
import org.nrg.testing.xnat.processing.files.comparators.imaging.metrics.PNormDistance;

import java.io.File;
import java.util.*;

public class DiffedImage {

    private boolean isColor;
    private ComparisonPixel[][][] signedComparisonPixels; // z, x, y so we can iterate over "pages"/"slices" (z)
    private static final Logger LOGGER = Logger.getLogger(DiffedImage.class);

    public boolean isColor() {
        return isColor;
    }

    public DiffedImage(File originalImageFile, File generatedImageFile) throws ImageProcessingException {
        ImagePlus original = openImage(originalImageFile);
        ImagePlus generated = openImage(generatedImageFile);
        if (original == null || generated == null) {
            throw new ImageProcessingException(String.format("Could not open image: %s", (original == null) ? originalImageFile.getName() : generatedImageFile.getName()));
        }
        if (!Arrays.equals(original.getDimensions(), generated.getDimensions())) {
            throw new ImageProcessingException(String.format("Original image dimensions of %s did not match generated image dimensions of %s in file %s.",
                    Arrays.toString(original.getDimensions()), Arrays.toString(generated.getDimensions()), generatedImageFile.getName()));
        }
        if (original.getType() != generated.getType()) {
            throw new ImageProcessingException("Original and generated images do not have the same color type for file: " + generatedImageFile.getName());
        }
        signedComparisonPixels = new ComparisonPixel[generated.getNSlices()][generated.getWidth()][generated.getHeight()];
        LOGGER.debug(String.format("Attempting to diff image with %d slice(s) and 2-D resolution %d by %d...", generated.getNSlices(), generated.getWidth(), generated.getHeight()));
        switch (generated.getType()) {
            case ImagePlus.GRAY8:
            case ImagePlus.GRAY16:
            case ImagePlus.GRAY32:
                for (int z = 0; z < generated.getNSlices(); z++) {
                    original.setSliceWithoutUpdate(z + 1);
                    generated.setSliceWithoutUpdate(z + 1);
                    for (int x = 0; x < generated.getWidth(); x++) {
                        for (int y = 0; y < generated.getHeight(); y++) {
                            signedComparisonPixels[z][x][y] = PixelFactory.getPixel(original.getPixel(x, y)[0], generated.getPixel(x, y)[0]);
                        }
                    }
                }
                isColor = false;
                break;
            case ImagePlus.COLOR_256:
            case ImagePlus.COLOR_RGB:
                for (int z = 0; z < generated.getNSlices(); z++) {
                    original.setSliceWithoutUpdate(z + 1);
                    generated.setSliceWithoutUpdate(z + 1);
                    for (int x = 0; x < generated.getWidth(); x++) {
                        for (int y = 0; y < generated.getHeight(); y++) {
                            int[] originalPixel = original.getPixel(x, y);
                            int[] generatedPixel = generated.getPixel(x, y);
                            signedComparisonPixels[z][x][y] = PixelFactory.getPixel(originalPixel[0], originalPixel[1], originalPixel[2], generatedPixel[0], generatedPixel[1], generatedPixel[2]);
                        }
                    }
                }
                isColor = true;
                break;
            default:
                throw new ImageProcessingException("Unknown color type for file: " + generatedImageFile.getName());
        }
        original.close();
        generated.close();
    }

    public DiffedImage(String originalImage, String generatedImage) throws ImageProcessingException {
        this(new File(originalImage), new File(generatedImage));
    }

    public List<ComparisonPixel> getNonzeroPixels() {
        final List<ComparisonPixel> pixels = new ArrayList<>();

        for (int z = 0; z < getPages(); z++) {
            pixels.addAll(getNonzeroPixels(z));
        }
        return pixels;
    }

    public List<ComparisonPixel> getNonzeroPixels(int z) {
        final List<ComparisonPixel> pixels = new ArrayList<>();
        for (int x = 0; x < signedComparisonPixels[0].length; x++) {
            for (int y = 0; y < signedComparisonPixels[0][0].length; y++) {
                final ComparisonPixel pixel = signedComparisonPixels[z][x][y];
                if (!(pixel instanceof ZeroComparisonPixel) && !(pixel instanceof ZeroDiffPixel)) {
                    pixel.setCoordinates(z, x, y);
                    pixels.add(pixel);
                }
            }
        }
        return pixels;
    }

    public int getPages() {
        return signedComparisonPixels.length;
    }

    public int getWidth() {
        return signedComparisonPixels[0].length;
    }

    public int getHeight() {
        return signedComparisonPixels[0][0].length;
    }

    public int getTotalNumPixels() {
        return getPages()*getWidth()*getHeight();
    }

    public ComparisonPixel getPixel(int slice, int x, int y) {
        return signedComparisonPixels[slice][x][y];
    }

    public ComparisonPixel getPixel(int x, int y) {
        return getPixel(0, x, y);
    }

    public int getAbsoluteDeviation() {
        return (int)Math.round(getTotalStackDifference(new PNormDistance(1)));
    }

    public double getSquaredDeviation() {
        return getTotalStackDifference(new PNormDistance(2));
    }

    public int getNumNonzeroPixels() {
        return (int)Math.round(getTotalStackDifference(new DiscreteMetric()));
    }

    public double getPercentNonzeroPixels() {
        return (100.0*getNumNonzeroPixels())/getTotalNumPixels(); // 100.0 to force double division
    }

    public double getTotalStackDifference(Metric metric) {
        double deviation = 0;
        for (int i = 0; i < getPages(); i++) {
            deviation += getTotalSliceDifference(metric, signedComparisonPixels[i]);
        }
        return deviation;
    }

    public Map<ComparisonPixel, Collection<ComparisonPixel>> nonzeroPixelAdjacencyGraph(int sliceNum) {
        final List<ComparisonPixel> nonzeroPixels = getNonzeroPixels(sliceNum);
        final Map<ComparisonPixel, Collection<ComparisonPixel>> adjacencyGraph = new HashMap<>();

        for (ComparisonPixel pixel : nonzeroPixels) {
            adjacencyGraph.put(pixel, new HashSet<ComparisonPixel>());
        }

        for (int i = 0; i < nonzeroPixels.size(); i++) {
            for (int j = i + 1; j < nonzeroPixels.size(); j++) {
                final ComparisonPixel pixel1 = nonzeroPixels.get(i);
                final ComparisonPixel pixel2 = nonzeroPixels.get(j);
                if (pixel1.isAdjacent(pixel2)) {
                    adjacencyGraph.get(pixel1).add(pixel2);
                    adjacencyGraph.get(pixel2).add(pixel1);
                }
            }
        }
        return adjacencyGraph;
    }

    public Set<Set<ComparisonPixel>> getConnectedComponents(int sliceNum) {
        return GraphUtils.findConnectedComponents(nonzeroPixelAdjacencyGraph(sliceNum));
    }

    public int getMaximalConnectedComponent(int sliceNum) {
        return GraphUtils.findMaximalConnectedComponent(nonzeroPixelAdjacencyGraph(sliceNum));
    }

    public int getMaximalConnectedComponent() {
        int max = 0;
        for (int i = 0; i < getPages(); i++) {
            max = Math.max(max, getMaximalConnectedComponent(i));
        }
        return max;
    }

    private double getTotalSliceDifference(Metric metric, ComparisonPixel[][] page) {
        double deviation = 0;
        for (ComparisonPixel[] pixels : page) {
            for (ComparisonPixel pixel : pixels) {
                try {
                    deviation += pixel.calculateDistance(metric);
                } catch (Exception e) {
                    throw new RuntimeException("Error in calculating distance: ", e);
                }
            }
        }
        return deviation;
    }

    private static ImagePlus openImage(File image) throws ImageProcessingException {
        try {
            ImagePlus readImage;
            if (image.getName().endsWith(".nii")) {
                NiftiReader niftiReader = new NiftiReader();
                niftiReader.setId(image.getPath());
                ImageProcessorReader processorReader = new ImageProcessorReader(niftiReader);
                ImageStack imageStack = new ImageStack(niftiReader.getSizeX(), niftiReader.getSizeY());
                for (int z = 0; z < niftiReader.getSizeZ(); z++) {
                    imageStack.addSlice(processorReader.openProcessors(z)[0]); // add each slice to stack
                }
                niftiReader.close();
                processorReader.close();
                readImage = new ImagePlus(image.getName(), imageStack);
            } else {
                Opener imageOpener = new Opener();
                readImage = imageOpener.openImage(image.getPath());
            }
            return readImage;
        } catch (Exception e) {
            throw new ImageProcessingException(String.format("Could not open image: %s due to: %s", image.getName(), e.getMessage()));
        }
    }
}
