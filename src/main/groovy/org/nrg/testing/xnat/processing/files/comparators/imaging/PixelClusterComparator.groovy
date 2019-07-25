package org.nrg.testing.xnat.processing.files.comparators.imaging

import groovy.transform.EqualsAndHashCode
import org.nrg.testing.xnat.processing.exceptions.ImageProcessingException

@EqualsAndHashCode
class PixelClusterComparator extends ImageComparator {

    int maxClusterSize

    @Override
    void checkDiffedImage(DiffedImage diffedImage) throws ImageProcessingException {
        final int maxCluster = diffedImage.maximalConnectedComponent
        if (maxCluster > maxClusterSize) {
            throw new ImageProcessingException("Cluster of ${maxCluster} differing pixels located, larger than the maximum allowed of ${maxClusterSize}")
        }
    }

}
