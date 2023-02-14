package org.nrg.testing.dicom.transform

import org.dcm4che3.data.DatasetWithFMI

import java.nio.file.Path

class OffsetIndexDicomWriter extends DefaultDicomWriter {

    int offset

    OffsetIndexDicomWriter(int offset) {
        this.offset = offset
    }

    @Override
    File writeDicom(DatasetWithFMI instance, Path dataDir, int fileIndex) {
        super.writeDicom(instance, dataDir, fileIndex + offset)
    }

}
