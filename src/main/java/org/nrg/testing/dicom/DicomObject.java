package org.nrg.testing.dicom;

import org.nrg.testing.dicom.values.*;

import java.util.HashMap;
import java.util.Map;

import static org.nrg.xnat.util.DicomUtils.stringHeaderToHexInt;

public class DicomObject extends DicomScopable {
    protected Map<DicomTag, DicomValue> dicomMap = new HashMap<>();

    public DicomObject(Map<DicomTag, DicomValue> dicomMap) {
        this.dicomMap = dicomMap;
    }

    public DicomObject() {}

    @Override
    public void markChildren() {
        for (Map.Entry<DicomTag, DicomValue> header : dicomMap.entrySet()) {
            header.getKey().setParent(this);
            header.getValue().setParent(header.getKey());
            header.getValue().markChildren();
        }
    }

    public Map<DicomTag, DicomValue> getDicomMap() {
        return dicomMap;
    }

    protected void put(int dicomHexTag, DicomValue dicomValue) {
        final DicomTag tag = new DicomTag(dicomHexTag);
        dicomMap.put(tag, dicomValue);
    }

    public void putExistenceChecks(int... dicomElements) {
        for (int dicomElement : dicomElements) {
            put(dicomElement, new DicomTagPresent());
        }
    }

    public void putExistenceChecks(String... dicomElements) {
        for (String dicomElement : dicomElements) {
            put(stringHeaderToHexInt(dicomElement), new DicomTagPresent());
        }
    }

    public void putNonexistenceChecks(int... dicomElements) {
        for (int dicomElement : dicomElements) {
            put(dicomElement, new DicomTagNotPresent());
        }
    }

    public void putNonexistenceChecks(String... dicomElements) {
        for (String dicomElement : dicomElements) {
            put(stringHeaderToHexInt(dicomElement), new DicomTagNotPresent());
        }
    }

    public void putWildcardedNonexistenceCheck(String wildcardedElement) {
        for (String concreteTag : DicomEditUtils.resolveAllDicomEditTags(wildcardedElement)) {
            putNonexistenceChecks(concreteTag);
        }
    }

    public void putValueEqualCheck(int dicomElement, String value) {
        put(dicomElement, new DicomTagHasValue(value));
    }

    public void putValueEqualCheck(String dicomElement, String value) {
        putValueEqualCheck(stringHeaderToHexInt(dicomElement), value);
    }

    public void putValueNotEqualCheck(int dicomElement, String value) {
        put(dicomElement, new DicomTagDoesntHaveValue(value));
    }

    public void putValueNotEqualCheck(String dicomElement, String value) {
        putValueNotEqualCheck(stringHeaderToHexInt(dicomElement), value);
    }

    public void putValueStartsWithCheck(int dicomElement, String value) {
        put(dicomElement, new DicomTagStartsWith(value));
    }

    public void putValueStartsWithCheck(String dicomElement, String value) {
        putValueStartsWithCheck(stringHeaderToHexInt(dicomElement), value);
    }

    public void putSequenceCheck(int dicomElement, DicomSequence sequence) {
        put(dicomElement, sequence);
    }

    public void putSequenceCheck(String dicomElement, DicomSequence sequence) {
        putSequenceCheck(stringHeaderToHexInt(dicomElement), sequence);
    }

    public DicomTag getTagByHexCode(int hexCode) {
            for (DicomTag tag : dicomMap.keySet()) {
            if (tag.asInt() == hexCode) {
                return tag;
            }
        }
        return null;
    }

    public DicomValue getValueByHexCode(int element) {
        for (Map.Entry<DicomTag, DicomValue> entry : dicomMap.entrySet()) {
            if (entry.getKey().asInt() == element) {
                return entry.getValue();
            }
        }
        return null;
    }

}
