package org.nrg.testing.tests;

import org.nrg.testing.dicom.DicomObject;
import org.nrg.testing.dicom.DicomScopable;
import org.nrg.testing.dicom.DicomTag;
import org.nrg.testing.dicom.RootDicomObject;
import org.nrg.testing.dicom.values.DicomSequence;
import org.nrg.testing.dicom.values.DicomValue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.nrg.testing.dicom.DicomLibrary.*;
import static org.testng.AssertJUnit.assertEquals;

public class DicomRepresentationTest {

    private DicomObject root;
    private DicomObject sequenceItem1;
    private DicomObject sequenceItem2;
    private DicomSequence dicomSequence;
    private DicomObject nestedSequenceItemA;
    private DicomSequence nestedSequenceA;
    private DicomObject nestedSequenceItemB;
    private DicomSequence nestedSequenceB;


    @BeforeClass
    public void setupDicomObject() {
        root = new RootDicomObject();
        root.putExistenceChecks(ACCESSION_NUMBER);
        root.putNonexistenceChecks(IMAGING_FREQUENCY);

        sequenceItem1 = new DicomObject();
        sequenceItem1.putExistenceChecks(ALLERGIES);
        sequenceItem2 = new DicomObject();
        sequenceItem2.putValueEqualCheck(MAGNETIC_FIELD_STRENGTH, "test");
        dicomSequence = new DicomSequence(sequenceItem1, sequenceItem2);
        root.putSequence(CODE_MEANING, dicomSequence);

        nestedSequenceItemA = new DicomObject();
        nestedSequenceItemA.putValueEqualCheck(DETECTOR_ID, "test123");
        nestedSequenceA = new DicomSequence(nestedSequenceItemA);

        nestedSequenceItemB = new DicomObject();
        nestedSequenceItemB.putSequence(INSTITUTIONAL_DEPARTMENT_NAME, nestedSequenceA);
        nestedSequenceB = new DicomSequence(nestedSequenceItemB);

        root.putSequence(PRIVATE_UNKNOWN_HEX, nestedSequenceB);

        root.markChildren();
    }

    @Test
    public void scopingTest() {
        final DicomValue detectorIdCheck = nestedSequenceItemA.getValueByElement(DETECTOR_ID);
        final List<DicomScopable> scope = detectorIdCheck.getFullScope();
        final DicomTag unknownHexTag = root.getTagByElement(PRIVATE_UNKNOWN_HEX);
        final DicomTag departmentNameTag = nestedSequenceItemB.getTagByElement(INSTITUTIONAL_DEPARTMENT_NAME);
        final DicomTag detectorIdTag = nestedSequenceItemA.getTagByElement(DETECTOR_ID);

        assertEquals(Arrays.asList(root, unknownHexTag, nestedSequenceB, nestedSequenceB.getItem(0), departmentNameTag, nestedSequenceA, nestedSequenceA.getItem(0), detectorIdTag, detectorIdCheck), scope);
    }

}
