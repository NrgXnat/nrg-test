package org.nrg.testing.dicom;

public class SequenceItem extends DicomObject {

    private int sequenceIndex;

    public int getSequenceIndex() {
        return sequenceIndex;
    }

    public void setSequenceIndex(int sequenceIndex) {
        this.sequenceIndex = sequenceIndex;
    }

    public SequenceItem(DicomObject dicomObject, int sequenceIndex) {
        super(dicomObject.dicomMap);
        this.sequenceIndex = sequenceIndex;
    }

}
