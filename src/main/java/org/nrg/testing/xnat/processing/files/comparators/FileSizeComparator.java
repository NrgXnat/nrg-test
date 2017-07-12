package org.nrg.testing.xnat.processing.files.comparators;

import org.nrg.testing.util.MathUtils;

import java.io.File;

public class FileSizeComparator extends FileComparator {

    private final long expectedSize;
    private final double tolerance;

    public FileSizeComparator(String params) {
        this(Long.parseLong(params.split(";")[0]), (params.split(";").length > 1) ? Double.parseDouble(params.split(";")[1]) : 0.0);
    }

    public FileSizeComparator(long expectedSize, double tolerance) {
        this.expectedSize = expectedSize;
        this.tolerance = tolerance;
    }

    @Override
    public String checkFileMatches(File file) {
        final long actualSize = file.length();
        if (actualSize == expectedSize) return null;
        final double error = MathUtils.percentError(expectedSize, actualSize);
        if (error > tolerance) {
            return (String.format("The file size for the file %s had a value of %d, which did not match the expected value of %d to within %f percent error.", file.getName(), actualSize, expectedSize, tolerance));
        }
        return null;
    }
}
