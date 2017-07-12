package org.nrg.testing.dicom.values;

import org.nrg.testing.dicom.DicomScopable;
import org.nrg.testing.dicom.DicomValidator;

public abstract class DicomValue extends DicomScopable {

    public abstract void assertValuesSatisfied(DicomValidator validator);

    @Override
    public void markChildren() {} // Only need this for sequences

}
