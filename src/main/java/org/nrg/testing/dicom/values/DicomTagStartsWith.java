package org.nrg.testing.dicom.values;

import org.nrg.testing.dicom.DicomValidator;

public class DicomTagStartsWith extends DicomTagValue {

    private final String value;

    public DicomTagStartsWith(String value) {
        this.value = (value == null) ? "" : value;
    }

    @Override
    public void assertValuesSatisfied(DicomValidator validator) {
        validator.checkTagStartsWith(getParent(), value);
    }


}
