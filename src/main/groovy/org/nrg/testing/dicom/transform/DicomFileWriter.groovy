package org.nrg.testing.dicom.transform

import org.dcm4che3.data.Attributes

import java.nio.file.Path

interface DicomFileWriter {

    File writeDicom(Attributes instance, Path dataDir, int fileIndex)

}
