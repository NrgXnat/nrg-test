package org.nrg.testing.dicom.values

import org.nrg.testing.dicom.DicomObject
import org.nrg.testing.dicom.DicomValidator
import org.nrg.testing.dicom.SequenceItem

class DicomSequence extends DicomValue {

    private final List<SequenceItem> items = []
    private boolean requestSizeCheck = true

    DicomSequence(DicomObject... objects) {
        objects.each { object ->
            addItem(object)
        }
    }

    List<SequenceItem> getItems() {
        items
    }

    SequenceItem getItem(int i) {
        items.get(i)
    }

    void addItem(DicomObject dicomObject) {
        items << new SequenceItem(dicomObject, items.size())
    }

    boolean requestSizeCheck() {
        requestSizeCheck
    }

    void setRequestSizeCheck(boolean requestSizeCheck) {
        this.requestSizeCheck = requestSizeCheck
    }

    DicomSequence disableSizeCheck() {
        setRequestSizeCheck(false)
        this
    }

    @Override
    void assertValuesSatisfied(DicomValidator validator) {
        validator.validateSequence(this)
    }

    @Override
    void markChildren() {
        items.each { sequenceItem ->
            sequenceItem.setParent(this)
            sequenceItem.markChildren()
        }
    }

}
