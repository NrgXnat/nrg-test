package org.nrg.testing.xnat.processing.files.comparators

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.nrg.testing.xnat.processing.exceptions.ProcessingValidationException
import org.nrg.testing.xnat.processing.files.comparators.imaging.ImageDeviationComparator
import org.nrg.testing.xnat.processing.files.comparators.imaging.NumberPixelsComparator
import org.nrg.testing.xnat.processing.files.comparators.imaging.PercentPixelsComparator
import org.nrg.testing.xnat.processing.files.comparators.imaging.PixelClusterComparator
import org.nrg.testing.xnat.processing.files.resources.ProcessingResourceFile

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = 'type')
@JsonSubTypes([
        @JsonSubTypes.Type(value = MD5_Comparator, name = 'MD5'),
        @JsonSubTypes.Type(value = ImageDeviationComparator, name = 'ImageDeviation'),
        @JsonSubTypes.Type(value = NumberPixelsComparator, name = 'NumPixels'),
        @JsonSubTypes.Type(value = PercentPixelsComparator, name = 'PercentPixels'),
        @JsonSubTypes.Type(value = PixelClusterComparator, name = 'Cluster'),
        @JsonSubTypes.Type(value = FileSizeComparator, name = 'FileSize'),
        @JsonSubTypes.Type(value = TextComparator, name = 'TextEquals')
])
abstract class FileComparator {

    abstract void checkFileMatches(File secondaryFileDirectory, File file, ProcessingResourceFile processingResourceFile) throws ProcessingValidationException

}