package org.nrg.testing.xnat.processing.files.comparators.imaging.metrics;

public class Metrics {
    public static final Metric DISCRETE = new DiscreteMetric();
    public static final Metric HAMMING = new HammingMetric();
    public static final Metric TAXICAB = new PNormDistance(1);
    public static final Metric EUCLIDEAN = new PNormDistance(2);
}
