package org.nrg.testing.dicom.values

import org.nrg.testing.dicom.DicomScopable
import org.nrg.testing.dicom.DicomValidator

abstract class DicomValue extends DicomScopable {

    abstract void assertValuesSatisfied(DicomValidator validator)

    @Override
    void markChildren() {} // Only need this for sequences

}
