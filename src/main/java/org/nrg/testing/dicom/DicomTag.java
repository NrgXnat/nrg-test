package org.nrg.testing.dicom;

public class DicomTag extends DicomScopable {

    private int decimalOfHexTag;

    @Override
    public void setParent(DicomScopable parent) {
        this.parent = parent;
        if (!(parent instanceof DicomObject)) throw new ClassCastException("DICOM tag can only have a DICOM object as its parent.");
    }

    public DicomTag(int tag) {
        decimalOfHexTag = tag;
    }

    public int getDecimalOfHexTag() {
        return decimalOfHexTag;
    }

    public String fullHexString() {
        final String hex = String.format("%08x", decimalOfHexTag);
        return "(" + hex.substring(0, 4) + "," + hex.substring(4) + ")";
    }

    @Override
    public void markChildren() {}

}
