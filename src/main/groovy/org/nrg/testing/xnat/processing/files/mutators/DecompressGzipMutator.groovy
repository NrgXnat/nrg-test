package org.nrg.testing.xnat.processing.files.mutators

import org.apache.commons.io.IOUtils

import java.util.zip.GZIPInputStream

class DecompressGzipMutator extends FileMutator {

    DecompressGzipMutator() {}

    @Override
    File mutateFile(File file) {
        final String name = file.path[0 .. -4] // trim off '.gz'
        final GZIPInputStream inputStream = new GZIPInputStream(new FileInputStream(file))
        final FileOutputStream outputStream = new FileOutputStream(name)
        IOUtils.copy(inputStream, outputStream)
        inputStream.close()
        outputStream.close()
        new File(name)
    }

}
