package org.nrg.testing.dicom

import org.dcm4che3.data.VR
import org.nrg.testing.dicom.values.*

import static org.nrg.testing.DicomUtils.stringHeaderToHexInt

class DicomObject extends DicomScopable {

    Map<DicomTag, DicomValue> dicomMap = [:]
    List<DicomTag> tagReferences = []

    DicomObject(Map<DicomTag, DicomValue> dicomMap, List<DicomTag> tagReferences) {
        setDicomMap(dicomMap)
        setTagReferences(tagReferences)
    }

    DicomObject(Map<DicomTag, DicomValue> dicomMap) {
        this(dicomMap, [])
    }

    DicomObject() {}

    @Override
    void markChildren() {
        dicomMap.each { tag, value ->
            tag.setParent(this)
            value.setParent(tag)
            value.markChildren()
        }
        tagReferences.each { tag ->
            tag.setParent(this)
        }
    }

    DicomTag getTagElement(String dicomElement) {
        final DicomTag dicomTag = new DicomTag(stringHeaderToHexInt(dicomElement))
        tagReferences << dicomTag
        dicomTag
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

    void putValueEqualCheck(int dicomElement, String value, VR expectedVr) {
        put(dicomElement, new DicomTagHasValue(value, expectedVr))
    }

    void putValueEqualCheck(String dicomElement, String value, VR expectedVr) {
        putValueEqualCheck(stringHeaderToHexInt(dicomElement), value, expectedVr)
    }

    void putValueEqualCheck(int dicomElement, DicomTag dicomTag) {
        put(dicomElement, new DicomTagHasValue(dicomTag))
    }

    void putValueEqualCheck(String dicomElement, DicomTag dicomTag) {
        putValueEqualCheck(stringHeaderToHexInt(dicomElement), dicomTag)
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
