package org.nrg.testing.dicom;

public class DicomTag extends DicomScopable {

    private int tagKey;

    @Override
    public void setParent(DicomScopable parent) {
        this.parent = parent;
        if (!(parent instanceof DicomObject)) throw new ClassCastException("DICOM tag can only have a DICOM object as its parent.");
    }

    public DicomTag(int tag) {
        tagKey = tag;
    }

    public int asInt() {
        return tagKey;
    }

    public String fullHexString() {
        final String hex = String.format("%08x", tagKey);
        return "(" + hex.substring(0, 4) + "," + hex.substring(4) + ")";
    }

    @Override
    public void markChildren() {}

}
