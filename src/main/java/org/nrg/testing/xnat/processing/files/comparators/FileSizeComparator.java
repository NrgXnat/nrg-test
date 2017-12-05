package org.nrg.testing.xnat.processing.files.comparators;

import org.nrg.testing.util.MathUtils;
import org.nrg.testing.xnat.processing.exceptions.FileValidationException;
import org.nrg.testing.xnat.processing.exceptions.ProcessingValidationException;
import org.nrg.testing.xnat.processing.files.resources.ProcessingResourceFile;

import java.io.File;

public class FileSizeComparator extends FileComparator {

    private double tolerance;

    public double getTolerance() {
        return tolerance;
    }

    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }

    @Override
    public void checkFileMatches(File secondaryFileDirectory, File file, ProcessingResourceFile processingResourceFile) throws ProcessingValidationException {
        final long actualSize = file.length();
        if (actualSize == processingResourceFile.getExpectedSize()) return;
        final double error = MathUtils.percentError(processingResourceFile.getExpectedSize(), actualSize);
        if (error > tolerance) {
            throw new FileValidationException(String.format("The file size for the file %s had a value of %d, which did not match the expected value of %d to within %f percent error.", file.getName(), actualSize, processingResourceFile.getExpectedSize(), tolerance));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileSizeComparator)) return false;

        FileSizeComparator that = (FileSizeComparator) o;

        return Double.compare(that.tolerance, tolerance) == 0;
    }

    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits(tolerance);
        return (int) (temp ^ (temp >>> 32));
    }

}
