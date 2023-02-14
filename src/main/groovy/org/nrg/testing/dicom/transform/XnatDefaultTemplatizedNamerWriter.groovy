package org.nrg.testing.dicom.transform

import org.dcm4che3.data.DatasetWithFMI
import org.dcm4che3.data.Tag
import org.nrg.testing.DicomUtils

import java.nio.file.Path

class XnatDefaultTemplatizedNamerWriter implements DicomFileWriter {

    @Override
    File writeDicom(DatasetWithFMI instance, Path dataDir, int fileIndex) {
        final File outputFile = dataDir.resolve(calculateNameFor(instance)).toFile()
        DicomUtils.writeDicomToFile(instance, outputFile)
        outputFile
    }

    String calculateNameFor(DatasetWithFMI instance) {
        final String simpleComponents = [Tag.StudyInstanceUID, Tag.SeriesNumber, Tag.InstanceNumber].collect { tag ->
            resolve(instance, tag)
        }.join('-')
        final String hashString = calculateHashString(instance)
        "${simpleComponents}-${hashString}.dcm"
    }

    private String resolve(DatasetWithFMI instance, int tag) {
        final String resolved = instance.dataset.getString(tag)
        if (resolved) {
            resolved
        } else {
            throw new UnsupportedOperationException('Missing values are not supported yet.')
        }
    }

    private String calculateHashString(DatasetWithFMI instance) {
        final int hash = [instance.dataset.getString(Tag.SOPClassUID), instance.dataset.getString(Tag.SOPInstanceUID)].hashCode()
        Long.toString(hash & 0xffffffffL, 36)
    }

}
