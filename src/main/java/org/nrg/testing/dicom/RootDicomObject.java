package org.nrg.testing.dicom;

import org.nrg.testing.dicom.values.DicomTagPresentNowhere;
import org.nrg.testing.dicom.values.DicomTagPresentSomewhere;

import java.util.Collections;
import java.util.List;

public class RootDicomObject extends DicomObject {

    @Override
    public void setParent(DicomScopable parent) {}

    @Override
    public DicomObject getParent() {
        return null;
    }

    @Override
    public List<DicomScopable> getFullScope(List<DicomScopable> partialList) {
        partialList.add(this);
        Collections.reverse(partialList);
        return partialList;
    }

    @Override
    public void validate(DicomValidator validator) {
        markChildren();
        super.validate(validator);
    }

    public void putPresentSomewhereCheck(DicomElement... dicomElements) {
        for (DicomElement dicomElement : dicomElements) {
            put(dicomElement, new DicomTagPresentSomewhere());
        }
    }

    public void putPresentNowhereCheck(DicomElement... dicomElements) {
        for (DicomElement dicomElement : dicomElements) {
            put(dicomElement, new DicomTagPresentNowhere());
        }
    }

}
