package org.nrg.testing.dicom.values;

import org.nrg.testing.dicom.DicomObject;
import org.nrg.testing.dicom.DicomValidator;
import org.nrg.testing.dicom.SequenceItem;

import java.util.ArrayList;
import java.util.List;

public class DicomSequence extends DicomValue {
    private final List<DicomObject> items = new ArrayList<>();

    public DicomSequence(DicomObject... objects) {
        for (int i = 0; i < objects.length; i++) {
            final DicomObject item = objects[i];
            final SequenceItem sequenceItem = new SequenceItem(item, i);
            items.add(sequenceItem);
        }
    }

    public DicomObject getItem(int i) {
        return items.get(i);
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
