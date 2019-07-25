package org.nrg.testing.dicom.values

import org.nrg.testing.dicom.DicomValidator

class DicomTagPresent extends DicomTagValue {

    @Override
    void assertValuesSatisfied(DicomValidator validator) {
        validator.checkTagPresent(getParent())
    }

}
