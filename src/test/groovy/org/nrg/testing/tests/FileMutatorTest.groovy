package org.nrg.testing.tests

import org.nrg.testing.FileIOUtils
import org.nrg.testing.xnat.processing.files.mutators.DecompressGzipMutator
import org.nrg.testing.xnat.processing.files.mutators.ReplaceAllMutator
import org.testng.annotations.Test

import java.nio.file.Files
import java.nio.file.Path

import static org.testng.AssertJUnit.*

class FileMutatorTest {

    private static final Path tempPath = Files.createTempDirectory('mutatortests')

    @Test
    void testReplaceAllMutator() {
        final File copiedFile = copyResourceToTemp('sample.log').toFile()
        final File mutatedFile = new ReplaceAllMutator(replacements : ['\\d{8}' : 'TIMESTAMP', 'email@website.com' : 'EMAIL']).mutateFile(copiedFile)
        assertEquals(copiedFile, mutatedFile)
        assertEquals(copiedFile.name, mutatedFile.name)
        assertEquals('LOG: TIMESTAMP inconsistency in divergence\nLOG: TIMESTAMP EMAIL requested update', copiedFile.text.trim())
    }

    @Test
    void testDecompressGzipMutator() {
        final Path copiedPath = copyResourceToTemp('file.txt.gz')
        final File mutatedFile = new DecompressGzipMutator().mutateFile(copiedPath.toFile())
        assertEquals(copiedPath.parent, mutatedFile.toPath().parent)
        assertEquals('file.txt', mutatedFile.name)
        assertEquals('Hello I am a file hiding in a gzipped archive.', mutatedFile.text.trim())
    }

    private Path copyResourceToTemp(String resourceName) {
        final File original = FileIOUtils.loadResource(resourceName)
        final Path copiedPath = tempPath.resolve(original.name)
        Files.copy(original.toPath(), copiedPath)
        copiedPath
    }

}
