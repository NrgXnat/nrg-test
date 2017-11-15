package org.nrg.testing.dicom.values;

import org.nrg.testing.dicom.DicomValidator;

public class DicomTagDoesntHaveValue extends DicomTagValue {

    private final String value;

    public DicomTagDoesntHaveValue(String value) {
        this.value = (value == null) ? "" : value;
    }

    @Override
    public void assertValuesSatisfied(DicomValidator validator) {
        validator.checkTagDoesntHaveValue(getParent(), value);
    }

}
