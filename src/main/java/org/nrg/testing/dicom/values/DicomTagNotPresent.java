package org.nrg.testing.dicom.values;

import org.nrg.testing.dicom.DicomValidator;

public class DicomTagNotPresent extends DicomTagValue {

    @Override
    public void assertValuesSatisfied(DicomValidator validator) {
        validator.checkTagNotPresent(getParent());
    }

}
