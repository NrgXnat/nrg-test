package org.nrg.testing.dicom;

import org.apache.commons.lang3.StringUtils;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.DatasetWithFMI;
import org.nrg.testing.dicom.values.DicomSequence;
import org.nrg.testing.dicom.values.DicomValue;
import org.nrg.xnat.util.DicomUtils;

import java.io.File;
import java.util.List;

import static org.testng.AssertJUnit.*;

public class DicomFileValidator extends DicomValidator {

    private DatasetWithFMI currentFullDicomData;

    @Override
    public void validate(File actualFile, DicomObject expectedDicomObject) {
        expectedDicomObject.markChildren();
        currentFullDicomData = DicomUtils.readDicom(actualFile);
        for (DicomValue dicomValue : expectedDicomObject.getDicomMap().values()) {
            dicomValue.assertValuesSatisfied(this);
        }
    }

    @Override
    public void checkTagPresent(DicomTag tag) {
        assertTrue("Could not find DicomElement:\n" + tag.getFullScopeStringRepresentation(), tagPresentInCorrectLocation(tag));
    }

    @Override
    public void checkTagNotPresent(DicomTag tag) {
        assertFalse("Found DicomElement when it should have been removed:\n" + tag.getFullScopeStringRepresentation(), tagPresentInCorrectLocation(tag));
    }

    @Override
    public void checkTagHasValue(DicomTag tag, String value) {
        checkTagPresent(tag);
        final String actual = StringUtils.join(expectedLocation(tag).getStrings(tag.asInt()), "\\");
        assertEquals(String.format("Found value '%s' instead of '%s' for DicomElement:\n%s", actual, value, tag.getFullScopeStringRepresentation()), value, (actual == null) ? "" : actual);
    }

    @Override
    public void checkTagDoesntHaveValue(DicomTag tag, String value) {
        checkTagPresent(tag);
        final String actual = expectedLocation(tag).getString(tag.asInt());
        assertFalse(String.format("Found value '%s' (but it should have been changed) for DicomElement:\n%s", actual, tag.getFullScopeStringRepresentation()), value.equals((actual == null) ? "" : actual));
    }

    @Override
    public void checkTagStartsWith(DicomTag tag, String value) {
        checkTagPresent(tag);
        final String actual = StringUtils.join(expectedLocation(tag).getStrings(tag.asInt()), "\\");
        assertTrue(String.format("Found value '%s' instead of value beginning with '%s' for DicomElement:\n%s", actual, value, tag.getFullScopeStringRepresentation()), actual.startsWith(value));
    }

    @Override
    public void validateSequence(DicomSequence sequence) {
        if (sequence.requestSizeCheck()) {
            final DicomTag parentTag = (DicomTag)sequence.getParent();
            assertEquals("Sequence did not have the expected number of sequence items. Path to sequence:\n" + parentTag.getFullScopeStringRepresentation(), sequence.getItems().size(), expectedLocation(parentTag).getSequence(parentTag.asInt()).size());
        }
        for (DicomObject item : sequence.getItems()) {
            for (DicomValue dicomValue : item.getDicomMap().values()) {
                dicomValue.assertValuesSatisfied(this);
            }
        }
    }

    private boolean tagPresentInCorrectLocation(DicomTag tag) {
        return expectedLocation(tag).contains(tag.asInt());
    }

    private Attributes expectedLocation(DicomTag tag) {
        if (tag.asInt() < 0x00080000) { // Metadata below (0008,0000)
            return currentFullDicomData.getFileMetaInformation(); // (0002,xxxx) elements should not be in sequences
        }

        final List<DicomScopable> scope = tag.getFullScope();
        Attributes currentDataset = currentFullDicomData.getDataset();
        for (int i = 0; i < scope.size() - 3; i += 3) { // predictable order in the scope: DicomObject -> Element -> Sequence -> SequenceItem (another DicomObject) -> Element -> Sequence -> SequenceItem ...
            currentDataset = currentDataset.getNestedDataset(((DicomTag)scope.get(i + 1)).asInt(), ((SequenceItem)scope.get(i + 3)).getSequenceIndex());
        }
        if (currentDataset != null ) {
            return currentDataset;
        } else {
            throw new AssertionError("Could not find parent in the DICOM source for DicomElement:\n" + tag.getFullScopeStringRepresentation());
        }
    }

}
