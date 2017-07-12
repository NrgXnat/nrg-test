package org.nrg.testing.dicom.values;

import org.nrg.testing.dicom.DicomValidator;

public class DicomTagPresentSomewhere extends DicomTagValue {

    @Override
    public void assertValuesSatisfied(DicomValidator validator) {
        validator.checkTagPresentSomewhere(getParent());
    }

}
