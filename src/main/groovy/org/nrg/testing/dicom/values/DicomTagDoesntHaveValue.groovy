package org.nrg.testing.dicom.values

import org.nrg.testing.dicom.DicomTag
import org.nrg.testing.dicom.DicomValidator

class DicomTagDoesntHaveValue extends DicomTagValue {

    private final String value

    DicomTagDoesntHaveValue(String value) {
        this.value = value ?: ''
    }

    @Override
    void assertValuesSatisfied(DicomValidator validator) {
        validator.checkTagDoesntHaveValue(getParent() as DicomTag, value)
    }

}
