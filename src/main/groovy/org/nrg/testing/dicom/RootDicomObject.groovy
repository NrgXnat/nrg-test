package org.nrg.testing.dicom

class RootDicomObject extends DicomObject {

    @Override
    void setParent(DicomScopable parent) {}

    @Override
    DicomObject getParent() {
        null
    }

    @Override
    List<DicomScopable> getFullScope(List<DicomScopable> partialList) {
        partialList << this
        partialList.reverse()
    }

}
