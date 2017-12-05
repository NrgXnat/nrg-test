package org.nrg.testing.xnat.processing.files.comparators;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.nrg.testing.xnat.processing.exceptions.ProcessingValidationException;
import org.nrg.testing.xnat.processing.files.comparators.imaging.*;
import org.nrg.testing.xnat.processing.files.resources.ProcessingResourceFile;

import java.io.File;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = MD5_Comparator.class, name = "MD5"),
        @JsonSubTypes.Type(value = ImageDeviationComparator.class, name = "ImageDeviation"),
        @JsonSubTypes.Type(value = NumberPixelsComparator.class, name = "NumPixels"),
        @JsonSubTypes.Type(value = PercentPixelsComparator.class, name = "PercentPixels"),
        @JsonSubTypes.Type(value = PixelClusterComparator.class, name = "Cluster"),
        @JsonSubTypes.Type(value = FileSizeComparator.class, name = "FileSize"),
        @JsonSubTypes.Type(value = TextComparator.class, name = "TextEquals")
})
public abstract class FileComparator {

    public abstract void checkFileMatches(File secondaryFileDirectory, File file, ProcessingResourceFile processingResourceFile) throws ProcessingValidationException;

}