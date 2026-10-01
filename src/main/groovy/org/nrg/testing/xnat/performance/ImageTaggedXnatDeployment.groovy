package org.nrg.testing.xnat.performance

import org.nrg.xnat.pogo.XnatDeployment

/**
 * A deployment named by a container image tag rather than by an XNAT version, for builds the version list doesn't
 * know, such as snapshots. Written {@code <image tag>=<deployment>} in {@code xnat.performance.deployments}: the
 * deployment part (an XNAT version, with any {@code +plugin}s) sets the version the tests treat it as, and the tag
 * names the image and labels the results.
 */
class ImageTaggedXnatDeployment extends XnatDeployment {

    String imageTag

    /** Reads one entry of {@code xnat.performance.deployments}, with or without an image tag. */
    static XnatDeployment parse(String specification) {
        final int separator = specification.indexOf('=')
        if (separator < 0) {
            return deploymentFromString(specification)
        }
        final String tag = specification.substring(0, separator).trim()
        if (!tag) {
            throw new IllegalArgumentException("No image tag before '=' in deployment ${specification}")
        }
        final XnatDeployment deployment = deploymentFromString(specification.substring(separator + 1).trim())
        final ImageTaggedXnatDeployment tagged = new ImageTaggedXnatDeployment()
        tagged.imageTag = tag
        tagged.id = tag
        tagged.xnatVersion = deployment.xnatVersion
        tagged.xnatVersionString = deployment.xnatVersionString
        tagged.plugins = deployment.plugins
        tagged
    }

    /** The image tag a deployment runs: its own tag if it has one, else its XNAT version. */
    static String imageTagFor(XnatDeployment deployment) {
        deployment instanceof ImageTaggedXnatDeployment ? deployment.imageTag : deployment.xnatVersionString
    }

}
