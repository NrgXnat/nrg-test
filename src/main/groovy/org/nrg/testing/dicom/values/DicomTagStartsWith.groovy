package org.nrg.testing.dicom.values

import org.nrg.testing.dicom.DicomValidator

class DicomTagStartsWith extends DicomTagValue {

    private final String value

    DicomTagStartsWith(String value) {
        this.value = value ?: ''
    }

    @Override
    void assertValuesSatisfied(DicomValidator validator) {
        validator.checkTagStartsWith(getParent(), value)
    }

}
