package org.nrg.testing.dicom.values;

import org.nrg.testing.dicom.DicomTag;

public abstract class DicomTagValue extends DicomValue {

    @Override
    public DicomTag getParent() {
        return (DicomTag)parent;
    }

}
