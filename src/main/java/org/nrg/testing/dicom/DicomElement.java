package org.nrg.testing.dicom;

import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.VR;
import org.dcm4che3.tool.common.CLIUtils;

public class DicomElement {

    private String groupNumber;
    private String elementNumber;
    private String dicomTag;
    private String name;
    private int dcm4cheTag;
    private VR vr;

    public DicomElement(String tag, String name) {
        dicomTag = tag.replace("(", "").replace(")", "");
        groupNumber = tag.split(",")[0];
        elementNumber = tag.split(",")[1];
        dicomTag = String.format("(%s,%s)", groupNumber, elementNumber);
        dcm4cheTag = CLIUtils.toTag(groupNumber + elementNumber);
        vr = ElementDictionary.vrOf(dcm4cheTag, null);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getDicomTag() {
        return dicomTag;
    }

    public int getDcm4cheTag() {
        return dcm4cheTag;
    }

    public VR getVr() {
        return vr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DicomElement that = (DicomElement) o;

        if (groupNumber != null ? !groupNumber.equals(that.groupNumber) : that.groupNumber != null) return false;
        return elementNumber != null ? elementNumber.equals(that.elementNumber) : that.elementNumber == null;
    }

    @Override
    public int hashCode() {
        int result = groupNumber != null ? groupNumber.hashCode() : 0;
        result = 31 * result + (elementNumber != null ? elementNumber.hashCode() : 0);
        return result;
    }

}
