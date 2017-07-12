package org.nrg.testing.dicom.values;

import org.nrg.testing.dicom.DicomValidator;

public class DicomTagPresentNowhere extends DicomTagValue {

    @Override
    public void assertValuesSatisfied(DicomValidator validator) {
        validator.checkTagPresentNowhere(getParent());
    }

}
