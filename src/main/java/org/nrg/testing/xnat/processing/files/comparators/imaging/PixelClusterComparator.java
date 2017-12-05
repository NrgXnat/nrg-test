package org.nrg.testing.xnat.processing.files.comparators.imaging;

import org.nrg.testing.xnat.processing.exceptions.ImageProcessingException;

public class PixelClusterComparator extends ImageComparator {

    private int maxClusterSize;

    public int getMaxClusterSize() {
        return maxClusterSize;
    }

    public void setMaxClusterSize(int maxClusterSize) {
        this.maxClusterSize = maxClusterSize;
    }

    @Override
    public void checkDiffedImage() throws ImageProcessingException {
        final int maxCluster = diffedImage.getMaximalConnectedComponent();
        if (maxCluster > maxClusterSize) {
            throw new ImageProcessingException(String.format("Cluster of %d differing pixels located, larger than the maximum allowed of %d", maxCluster, maxClusterSize));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PixelClusterComparator)) return false;

        PixelClusterComparator that = (PixelClusterComparator) o;

        return maxClusterSize == that.maxClusterSize;
    }

    @Override
    public int hashCode() {
        return maxClusterSize;
    }

}
