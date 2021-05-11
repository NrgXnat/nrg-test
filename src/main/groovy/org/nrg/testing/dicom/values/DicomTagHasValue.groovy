package org.nrg.testing.dicom.values

import org.dcm4che3.data.VR
import org.nrg.testing.dicom.DicomTag
import org.nrg.testing.dicom.DicomValidator

class DicomTagHasValue extends DicomTagValue {

    private String value
    private DicomTag otherTag
    private VR expectedVr

    DicomTagHasValue(String value) {
        this.value = value ?: ''
    }

    DicomTagHasValue(DicomTag otherTag) {
        this.otherTag = otherTag
    }

    DicomTagHasValue(String value, VR expectedVr) {
        this(value)
        this.expectedVr = expectedVr
    }

    @Override
    void assertValuesSatisfied(DicomValidator validator) {
        if (otherTag != null) {
            validator.checkTagHasValue(getParent() as DicomTag, otherTag)
        } else if (expectedVr != null) {
            validator.checkTagHasValue(getParent() as DicomTag, value, expectedVr)
        } else {
            validator.checkTagHasValue(getParent() as DicomTag, value)
        }
    }

}
