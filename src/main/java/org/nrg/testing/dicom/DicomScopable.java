package org.nrg.testing.dicom;

import java.util.ArrayList;
import java.util.List;

public abstract class DicomScopable {

    protected DicomScopable parent;

    public void setParent(DicomScopable parent) {
        this.parent = parent;
    }

    public DicomScopable getParent() {
        return parent;
    }

    public abstract void markChildren();

    public List<DicomScopable> getFullScope(List<DicomScopable> partialList) {
        if (partialList == null) {
            final List<DicomScopable> result = new ArrayList<>();
            result.add(this);
            return getParent().getFullScope(result);
        } else {
            partialList.add(this);
            return getParent().getFullScope(partialList);
        }
    }

    public List<DicomScopable> getFullScope() {
        return getFullScope(null);
    }

}
