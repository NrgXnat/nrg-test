package org.nrg.testing.dicom;

import org.nrg.testing.dicom.values.*;

import java.util.HashMap;
import java.util.Map;

public class DicomObject extends DicomScopable {
    protected Map<DicomTag, DicomValue> dicomMap = new HashMap<>();

    public DicomObject(Map<DicomTag, DicomValue> dicomMap) {
        this.dicomMap.putAll(dicomMap);
    }

    public DicomObject() {}

    public void validate(DicomValidator validator) {
        for (DicomValue value : dicomMap.values()) {
            value.assertValuesSatisfied(validator);
        }
    }

    @Override
    public void markChildren() {
        for (Map.Entry<DicomTag, DicomValue> header : dicomMap.entrySet()) {
            header.getKey().setParent(this);
            header.getValue().setParent(header.getKey());
            header.getValue().markChildren();
        }
    }

    protected void put(DicomElement dicomElement, DicomValue dicomValue) {
        final DicomTag tag = new DicomTag(dicomElement);
        dicomMap.put(tag, dicomValue);
    }

    public void putExistenceChecks(DicomElement... dicomElements) {
        for (DicomElement dicomElement : dicomElements) {
            put(dicomElement, new DicomTagPresent());
        }
    }

    public void putNonexistenceChecks(DicomElement... dicomElements) {
        for (DicomElement dicomElement : dicomElements) {
            put(dicomElement, new DicomTagNotPresent());
        }
    }

    public void putValueEqualCheck(DicomElement dicomElement, String value) {
        put(dicomElement, new DicomTagHasValue(value));
    }

    public void putValueNotEqualCheck(DicomElement dicomElement, String value) {
        put(dicomElement, new DicomTagDoesntHaveValue(value));
    }

    public void putSequence(DicomElement dicomElement, DicomSequence sequence) {
        put(dicomElement, sequence);
    }

    public DicomTag getTagByElement(DicomElement element) {
        for (DicomTag tag : dicomMap.keySet()) {
            if (tag.getDicomElement().equals(element)) {
                return tag;
            }
        }
        return null;
    }

    public DicomValue getValueByElement(DicomElement element) {
        for (Map.Entry<DicomTag, DicomValue> entry : dicomMap.entrySet()) {
            if (entry.getKey().getDicomElement().equals(element)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /*public String getSummary(String header) {
        String summary = header;
        List<String> presentTags = new ArrayList<>();
        List<String> nonpresentTags = new ArrayList<>();
        String valueString = "";
        String notvalueString = "";

        for (Map.Entry<DicomTag, DicomValue> entry : dicomMap.entrySet()) {
            switch (entry.getValue().getType()) {
                case PRESENT:
                    presentTags.add(entry.getKey().getDicomTag());
                    break;
                case NOT_PRESENT:
                    nonpresentTags.add(entry.getKey().getDicomTag());
                    break;
                case HAS_VALUE:
                    valueString += String.format("\n\t* DICOM header %s has value: \"%s\"", entry.getKey().getDicomTag(), entry.getValue().getValue());
                    break;
                case DOESNT_HAVE_VALUE:
                    notvalueString += String.format("\n\t* DICOM header %s doesn't have value: \"%s\"", entry.getKey().getDicomTag(), entry.getValue().getValue());
                    break;
            }
        }
        if (!presentTags.isEmpty()) summary += "\n\t* The following DICOM headers are all present: " + StringUtils.join(presentTags.toArray(), ", ");
        if (!nonpresentTags.isEmpty()) summary += "\n\t* The following DICOM headers are all not present: " + StringUtils.join(nonpresentTags.toArray(), ", ");
        summary += valueString;
        summary += notvalueString;
        return summary;
    }*/
    // TODO:above

}
