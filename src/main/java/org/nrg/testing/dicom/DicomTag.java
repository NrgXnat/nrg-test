package org.nrg.testing.dicom;

public class DicomTag extends DicomScopable {

    private DicomElement tag;

    @Override
    public void setParent(DicomScopable parent) {
        this.parent = parent;
        if (!(parent instanceof DicomObject)) throw new ClassCastException("DICOM tag can only have a DICOM object as it's parent.");
    }

    public DicomTag(DicomElement tag) {
        this.tag = tag;
    }

    public DicomElement getDicomElement() {
        return tag;
    }

    @Override
    public void markChildren() {}

}
