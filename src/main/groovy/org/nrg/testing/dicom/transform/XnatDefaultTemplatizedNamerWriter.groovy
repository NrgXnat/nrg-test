package org.nrg.testing.dicom.transform

import org.dcm4che3.data.Attributes
import org.dcm4che3.data.Tag
import org.nrg.testing.DicomUtils

import java.nio.file.Path

class XnatDefaultTemplatizedNamerWriter implements DicomFileWriter {

    @Override
    File writeDicom(Attributes instance, Path dataDir, int fileIndex) {
        final File outputFile = dataDir.resolve(calculateNameFor(instance)).toFile()
        DicomUtils.writeDicomToFile(instance, outputFile)
        outputFile
    }

    String calculateNameFor(Attributes instance) {
        final String simpleComponents = [Tag.StudyInstanceUID, Tag.SeriesNumber, Tag.InstanceNumber].collect { tag ->
            resolve(instance, tag)
        }.join('-')
        final String hashString = calculateHashString(instance)
        "${simpleComponents}-${hashString}.dcm"
    }

    private String resolve(Attributes instance, int tag) {
        final String resolved = instance.getString(tag)
        if (resolved) {
            resolved
        } else {
            throw new UnsupportedOperationException('Missing values are not supported yet.')
        }
    }

    private String calculateHashString(Attributes instance) {
        final int hash = [instance.getString(Tag.SOPClassUID), instance.getString(Tag.SOPInstanceUID)].hashCode()
        Long.toString(hash & 0xffffffffL, 36)
    }

}
