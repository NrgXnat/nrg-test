package org.nrg.testing.dicom

class SequenceItem extends DicomObject {

    int sequenceIndex

    SequenceItem(DicomObject dicomObject, int sequenceIndex) {
        super(dicomObject.dicomMap, dicomObject.tagReferences)
        setSequenceIndex(sequenceIndex)
    }

}
