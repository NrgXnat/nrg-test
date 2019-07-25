package org.nrg.testing.dicom.values

import org.nrg.testing.dicom.DicomValidator

class DicomTagNotPresent extends DicomTagValue {

    @Override
    void assertValuesSatisfied(DicomValidator validator) {
        validator.checkTagNotPresent(getParent())
    }

}
