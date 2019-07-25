package org.nrg.testing.dicom

import org.nrg.testing.dicom.values.*

import static org.nrg.testing.DicomUtils.stringHeaderToHexInt

class DicomObject extends DicomScopable {

    Map<DicomTag, DicomValue> dicomMap = [:]

    DicomObject(Map<DicomTag, DicomValue> dicomMap) {
        setDicomMap(dicomMap)
    }

    DicomObject() {}

    @Override
    void markChildren() {
        dicomMap.each { tag, value ->
            tag.setParent(this)
            value.setParent(tag)
            value.markChildren()
        }
    }

    protected void put(int dicomHexTag, DicomValue dicomValue) {
        dicomMap.put(new DicomTag(dicomHexTag), dicomValue)
    }

    void putExistenceChecks(int... dicomElements) {
        dicomElements.each { dicomElement ->
            put(dicomElement, new DicomTagPresent())
        }
    }

    void putExistenceChecks(String... dicomElements) {
        dicomElements.each { dicomElement ->
            put(stringHeaderToHexInt(dicomElement), new DicomTagPresent())
        }
    }

    void putNonexistenceChecks(int... dicomElements) {
        dicomElements.each { dicomElement ->
            put(dicomElement, new DicomTagNotPresent())
        }
    }

    void putNonexistenceChecks(String... dicomElements) {
        dicomElements.each { dicomElement ->
            put(stringHeaderToHexInt(dicomElement), new DicomTagNotPresent())
        }
    }

    void putWildcardedNonexistenceCheck(String wildcardedElement) {
        DicomEditUtils.resolveAllDicomEditTags(wildcardedElement).each { concreteTag ->
            putNonexistenceChecks(concreteTag)
        }
    }

    void putValueEqualCheck(int dicomElement, String value) {
        put(dicomElement, new DicomTagHasValue(value))
    }

    void putValueEqualCheck(String dicomElement, String value) {
        putValueEqualCheck(stringHeaderToHexInt(dicomElement), value)
    }

    void putValueNotEqualCheck(int dicomElement, String value) {
        put(dicomElement, new DicomTagDoesntHaveValue(value))
    }

    void putValueNotEqualCheck(String dicomElement, String value) {
        putValueNotEqualCheck(stringHeaderToHexInt(dicomElement), value)
    }

    void putValueStartsWithCheck(int dicomElement, String value) {
        put(dicomElement, new DicomTagStartsWith(value))
    }

    void putValueStartsWithCheck(String dicomElement, String value) {
        putValueStartsWithCheck(stringHeaderToHexInt(dicomElement), value)
    }

    void putSequenceCheck(int dicomElement, DicomSequence sequence) {
        put(dicomElement, sequence)
    }

    void putSequenceCheck(String dicomElement, DicomSequence sequence) {
        putSequenceCheck(stringHeaderToHexInt(dicomElement), sequence)
    }

    DicomTag getTagByHexCode(int hexCode) {
        dicomMap.keySet().find { tag ->
            tag.asInt() == hexCode
        }
    }

    DicomValue getValueByHexCode(int element) {
        final DicomTag hexSearch = getTagByHexCode(element)
        (hexSearch != null) ? dicomMap[hexSearch] : null
    }

}
