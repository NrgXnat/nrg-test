package org.nrg.testing.dicom.transform

import org.dcm4che3.data.DatasetWithFMI
import org.nrg.testing.DicomUtils

import java.nio.file.Path

class DefaultDicomWriter implements DicomFileWriter {

    @Override
    void writeDicom(DatasetWithFMI instance, Path dataDir, int fileIndex) {
        DicomUtils.writeDicomToFile(instance, dataDir.resolve("${fileIndex}.dcm").toFile())
    }

}
