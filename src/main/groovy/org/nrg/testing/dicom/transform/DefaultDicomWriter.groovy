package org.nrg.testing.dicom.transform

import org.dcm4che3.data.DatasetWithFMI
import org.nrg.testing.DicomUtils

import java.nio.file.Path

class DefaultDicomWriter implements DicomFileWriter {

    @Override
    File writeDicom(DatasetWithFMI instance, Path dataDir, int fileIndex) {
        final File outputFile = dataDir.resolve("${fileIndex}.dcm").toFile()
        DicomUtils.writeDicomToFile(instance, outputFile)
        outputFile
    }

}
