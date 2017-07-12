package org.nrg.testing.dicom.values;

import org.nrg.testing.dicom.DicomValidator;

public class DicomTagHasValue extends DicomTagValue {

    private final String value;

    public DicomTagHasValue(String value) {
        this.value = value;
    }

    @Override
    public void assertValuesSatisfied(DicomValidator validator) {
        validator.checkTagHasValue(getParent(), value);
    }

}
