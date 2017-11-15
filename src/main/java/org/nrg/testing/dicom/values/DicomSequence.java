package org.nrg.testing.dicom.values;

import org.nrg.testing.dicom.DicomObject;
import org.nrg.testing.dicom.DicomValidator;
import org.nrg.testing.dicom.SequenceItem;

import java.util.ArrayList;
import java.util.List;

public class DicomSequence extends DicomValue {

    private final List<SequenceItem> items = new ArrayList<>();
    private boolean requestSizeCheck = false;

    public DicomSequence(DicomObject... objects) {
        for (DicomObject object : objects) {
            addItem(object);
        }
    }

    public List<SequenceItem> getItems() {
        return items;
    }

    public SequenceItem getItem(int i) {
        return items.get(i);
    }

    public void addItem(DicomObject dicomObject) {
        items.add(new SequenceItem(dicomObject, items.size()));
    }

    public boolean requestSizeCheck() {
        return requestSizeCheck;
    }

    public void setRequestSizeCheck(boolean requestSizeCheck) {
        this.requestSizeCheck = requestSizeCheck;
    }

    public DicomSequence disableSizeCheck() {
        setRequestSizeCheck(false);
        return this;
    }

    @Override
    public void assertValuesSatisfied(DicomValidator validator) {
        validator.validateSequence(this);
    }

    @Override
    public void markChildren() {
        for (DicomObject sequenceItem : items) {
            sequenceItem.setParent(this);
            sequenceItem.markChildren();
        }
    }

}
