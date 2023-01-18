package org.nrg.testing.dicom.transform

import org.dcm4che3.data.DatasetWithFMI

import java.nio.file.Path

interface DicomFileWriter {

    File writeDicom(DatasetWithFMI instance, Path dataDir, int fileIndex)

}
