package org.nrg.testing.xnat.processing.files.comparators.imaging.metrics

import org.nrg.testing.xnat.processing.files.comparators.imaging.ComparisonPixel

/**
 * Should define a metric: https://en.wikipedia.org/wiki/Metric_(mathematics) on a ComparisonPixel that handles if the pixel pair is colored or grayscale (i.e. R^3 or R)
 */
abstract class Metric {

    abstract double distance(ComparisonPixel pixel)

}
