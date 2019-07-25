package org.nrg.testing.dicom

import org.dcm4che3.data.Attributes
import org.dcm4che3.data.DatasetWithFMI
import org.nrg.testing.DicomUtils
import org.nrg.testing.dicom.values.DicomSequence

import static org.testng.AssertJUnit.*

class DicomFileValidator extends DicomValidator {

    private DatasetWithFMI currentFullDicomData

    @Override
    void validate(File actualFile, DicomObject expectedDicomObject) {
        expectedDicomObject.markChildren()
        currentFullDicomData = DicomUtils.readDicom(actualFile)
        expectedDicomObject.dicomMap.values().each { dicomValue ->
            dicomValue.assertValuesSatisfied(this)
        }
    }

    @Override
    void checkTagPresent(DicomTag tag) {
        assertTrue("Could not find DicomElement:\n${tag.fullScopeStringRepresentation}", tagPresentInCorrectLocation(tag))
    }

    @Override
    void checkTagNotPresent(DicomTag tag) {
        assertFalse("Found DicomElement when it should have been removed:\n${tag.fullScopeStringRepresentation}", tagPresentInCorrectLocation(tag))
    }

    @Override
    void checkTagHasValue(DicomTag tag, String value) {
        checkTagPresent(tag)
        final String actual = expectedLocation(tag).getStrings(tag.asInt()).join('\\')
        assertEquals("Found value '${actual}' instead of '${value}' for DicomElement:\n${tag.fullScopeStringRepresentation}",
                value,
                actual ?: ''
        )
    }

    @Override
    void checkTagDoesntHaveValue(DicomTag tag, String value) {
        checkTagPresent(tag)
        final String actual = expectedLocation(tag).getString(tag.asInt()) ?: ''
        assertFalse("Found value '${actual}' (but it should have been changed) for DicomElement:\n%${tag.fullScopeStringRepresentation}", value == actual)
    }

    @Override
    void checkTagStartsWith(DicomTag tag, String value) {
        checkTagPresent(tag)
        final String actual = expectedLocation(tag).getStrings(tag.asInt()).join('\\')
        assertTrue("Found value '${actual}' instead of value beginning with '${value}' for DicomElement:\n${tag.fullScopeStringRepresentation}", actual.startsWith(value))
    }

    @Override
    void validateSequence(DicomSequence sequence) {
        if (sequence.requestSizeCheck()) {
            final DicomTag parentTag = sequence.parent as DicomTag
            assertEquals("Sequence did not have the expected number of sequence items. Path to sequence:\n${parentTag.fullScopeStringRepresentation}",
                    sequence.items.size(),
                    expectedLocation(parentTag).getSequence(parentTag.asInt()).size()
            )
        }
        sequence.items.each { item ->
            item.dicomMap.values().each { dicomValue ->
                dicomValue.assertValuesSatisfied(this)
            }
        }
    }

    private boolean tagPresentInCorrectLocation(DicomTag tag) {
        expectedLocation(tag).contains(tag.asInt())
    }

    private Attributes expectedLocation(DicomTag tag) {
        if (tag.asInt() < 0x00080000) { // Metadata below (0008,0000)
            return currentFullDicomData.fileMetaInformation // (0002,xxxx) elements should not be in sequences
        }

        final List<DicomScopable> scope = tag.fullScope
        Attributes currentDataset = currentFullDicomData.dataset
        if (scope.size() > 3) {
            (0 .. scope.size() - 3).step(3).each { i -> // predictable order in the scope: DicomObject -> Element -> Sequence -> SequenceItem (another DicomObject) -> Element -> Sequence -> SequenceItem ...
                currentDataset = currentDataset.getNestedDataset((scope[i + 1] as DicomTag).asInt(), (scope[i + 3] as SequenceItem).sequenceIndex)
            }
        }
        if (currentDataset != null ) {
            currentDataset
        } else {
            throw new AssertionError("Could not find parent in the DICOM source for DicomElement:\n${tag.fullScopeStringRepresentation}")
        }
    }

}
