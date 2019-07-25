package org.nrg.testing.dicom

import org.nrg.testing.DicomUtils

class DicomTag extends DicomScopable {

    private int tagKey

    @Override
    void setParent(DicomScopable parent) {
        super.setParent(parent)
        if (!(parent instanceof DicomObject)) throw new ClassCastException('DICOM tag can only have a DICOM object as its parent.')
    }

    DicomTag(int tag) {
        tagKey = tag
    }

    int asInt() {
        tagKey
    }

    String fullHexString() {
        DicomUtils.intToFullHexString(tagKey)
    }

    @Override
    void markChildren() {}

}
