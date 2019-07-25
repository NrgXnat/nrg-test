package org.nrg.testing.dicom.values

import org.nrg.testing.dicom.DicomTag
import org.nrg.testing.dicom.DicomValidator

class DicomTagHasValue extends DicomTagValue {

    private final String value

    DicomTagHasValue(String value) {
        this.value = value ?: ''
    }

    @Override
    void assertValuesSatisfied(DicomValidator validator) {
        validator.checkTagHasValue(getParent() as DicomTag, value)
    }

}
