package org.nrg.testing.tests

import org.nrg.testing.FileIOUtils
import org.nrg.testing.UnitTestUtils
import org.nrg.testing.xnat.processing.files.AssessorFileSet
import org.nrg.testing.xnat.processing.files.ProcessingFileSets
import org.nrg.testing.xnat.processing.files.comparators.FileComparator
import org.nrg.testing.xnat.processing.files.comparators.FileSizeComparator
import org.nrg.testing.xnat.processing.files.comparators.MD5_Comparator
import org.nrg.testing.xnat.processing.files.comparators.TextComparator
import org.nrg.testing.xnat.processing.files.comparators.imaging.ImageDeviationComparator
import org.nrg.testing.xnat.processing.files.comparators.imaging.NumberPixelsComparator
import org.nrg.testing.xnat.processing.files.comparators.imaging.PercentPixelsComparator
import org.nrg.testing.xnat.processing.files.comparators.imaging.PixelClusterComparator
import org.nrg.testing.xnat.processing.files.jackson.module.ProcessingFileJacksonModule
import org.nrg.testing.xnat.processing.files.mutators.DecompressGzipMutator
import org.nrg.testing.xnat.processing.files.mutators.FileMutator
import org.nrg.testing.xnat.processing.files.mutators.ReplaceAllMutator
import org.nrg.testing.xnat.processing.files.resources.ProcessingResource
import org.nrg.testing.xnat.processing.files.resources.ProcessingResourceFile
import org.nrg.xnat.jackson.mappers.YamlObjectMapper
import org.nrg.xnat.pogo.DataType
import org.testng.annotations.Test

import static org.testng.AssertJUnit.*

class ProcessingFileSetsTest {

    @Test
    void testProcessingFileSetsDeserialization() {
        final ProcessingFileSets processingFileSets = new YamlObjectMapper().registerModule(new ProcessingFileJacksonModule()).readValue(FileIOUtils.loadResource('processing_file_sets_example.yaml'), ProcessingFileSets)
        assertTrue(processingFileSets instanceof AssessorFileSet)
        assertEquals(DataType.QC.xsiType, (processingFileSets as AssessorFileSet).xsiType)

        final List<ProcessingResource> processingResources = processingFileSets.resources
        assertEquals(2, processingResources.size())

        final ProcessingResource logResource = processingResources[0]
        assertEquals('LOG', logResource.folder)
        final List<ProcessingResourceFile> logFiles = logResource.processingFiles()
        assertEquals(1, logFiles.size())
        final ProcessingResourceFile logFile = logFiles[0]
        assertEquals('logfile.log', logFile.name)

        final ProcessingResource dataResource = processingResources[1]
        assertEquals('DATA_\\d{14}', dataResource.folder)
        assertTrue(dataResource.isRegex)
        assertEquals('QC_files', dataResource.secondaryResources)
        final List<ProcessingResourceFile> dataFiles = dataResource.processingFiles()
        assertEquals(9, dataFiles.size())
        final ProcessingResourceFile timestampedFile = dataFiles[0]
        checkFileNameAndComparator(timestampedFile, 'generated_values.txt', 'text_equals')
        assertEquals('timestamp', timestampedFile.mutator)
        assertEquals('44.4 | 55.5 | 99.9 | DATE', timestampedFile.expectedText)
        final ProcessingResourceFile snapshot = dataFiles[1]
        checkFileNameAndComparator(snapshot, 'snapshot.png', 'image_deviation')
        assertEquals('original_snapshot.png', snapshot.compareTo)
        final ProcessingResourceFile gzippedNifti = dataFiles[2]
        checkFileNameAndComparator(gzippedNifti, 'generated.nii.gz', 'pixel_count')
        assertEquals('ungzip', gzippedNifti.mutator)
        final ProcessingResourceFile overlayRegexFile = dataFiles[4]
        checkFileNameAndComparator(overlayRegexFile, 'scan_overlay\\d{5}.jpg', 'cluster')
        assertTrue(overlayRegexFile.isRegex())
        assertEquals('otherdata1.txt', dataFiles[7].name) // nothing else interesting in the skipped files
        assertEquals('otherdata2.txt', dataFiles[8].name)

        final Map<String, FileMutator> mutators = processingFileSets.mutators
        assertEquals(2, mutators.size())
        assertTrue(mutators['ungzip'] instanceof DecompressGzipMutator)
        final FileMutator replaceAllMutator = mutators['timestamp']
        assertTrue(replaceAllMutator instanceof ReplaceAllMutator)
        assertEquals(['\\d{8}' : 'DATE'], (replaceAllMutator as ReplaceAllMutator).replacements)

        final Map<String, FileComparator> comparators = processingFileSets.comparators
        assertEquals(7, comparators.size())
        final ImageDeviationComparator imageDeviationComparator = comparators['image_deviation'] as ImageDeviationComparator
        assertEquals(5000, imageDeviationComparator.maxGrayscaleDeviation)
        assertEquals(20000, imageDeviationComparator.maxColorscaleDeviation)
        final NumberPixelsComparator numPixels = comparators['pixel_count'] as NumberPixelsComparator
        assertEquals(100, numPixels.maxDifferingPixels)
        final PercentPixelsComparator percentPixels = comparators['pixel_percent'] as PercentPixelsComparator
        UnitTestUtils.assertDoubleEqual(1.0, percentPixels.maxPercentError)
        final PixelClusterComparator clusterComparator = comparators['cluster'] as PixelClusterComparator
        assertEquals(5, clusterComparator.maxClusterSize)
        final FileSizeComparator sizeComparator = comparators['file_size'] as FileSizeComparator
        UnitTestUtils.assertDoubleEqual(1.0, sizeComparator.tolerance)
        assertTrue(comparators['md5'] instanceof MD5_Comparator)
        assertTrue(comparators['text_equals'] instanceof TextComparator)
    }

    private void checkFileNameAndComparator(ProcessingResourceFile processingResourceFile, String name, String comparator) {
        assertEquals(name, processingResourceFile.name)
        assertEquals(comparator, processingResourceFile.comparator)
    }

}
