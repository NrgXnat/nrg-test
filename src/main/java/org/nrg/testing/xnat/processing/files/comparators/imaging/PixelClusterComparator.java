package org.nrg.testing.xnat.processing.files.comparators.imaging;

import org.nrg.testing.xnat.processing.files.ImageProcessingException;

public class PixelClusterComparator extends ImageComparator {

    private int maxClusterSize;

    public PixelClusterComparator(String pixels) {
        maxClusterSize = Integer.parseInt(pixels);
    }

    @Override
    public String checkDiffedImage() throws ImageProcessingException {
        final int maxCluster = diffedImage.getMaximalConnectedComponent();
        if (maxCluster > maxClusterSize) {
            return String.format("Cluster of %d differing pixels located, larger than the maximum allowed of %d", maxCluster, maxClusterSize);
        } else {
            return null;
        }
    }

}
