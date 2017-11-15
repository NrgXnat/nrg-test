package org.nrg.testing.dicom;

import java.util.Collections;
import java.util.List;

public class RootDicomObject extends DicomObject {

    @Override
    public void setParent(DicomScopable parent) {}

    @Override
    public DicomObject getParent() {
        return null;
    }

    @Override
    public List<DicomScopable> getFullScope(List<DicomScopable> partialList) {
        partialList.add(this);
        Collections.reverse(partialList);
        return partialList;
    }

}
