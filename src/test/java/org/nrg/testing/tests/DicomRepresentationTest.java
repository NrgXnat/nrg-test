package org.nrg.testing.tests;

import org.nrg.testing.dicom.*;
import org.nrg.testing.dicom.values.DicomSequence;
import org.nrg.testing.dicom.values.DicomValue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.dcm4che3.data.Tag.*;
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
        root.putExistenceChecks(AccessionNumber);
        root.putNonexistenceChecks(ImagingFrequency);

        sequenceItem1 = new DicomObject();
        sequenceItem1.putExistenceChecks(Allergies);
        sequenceItem2 = new DicomObject();
        sequenceItem2.putValueEqualCheck(MagneticFieldStrength, "test");
        dicomSequence = new DicomSequence(sequenceItem1, sequenceItem2);
        root.putSequenceCheck(CodeMeaning, dicomSequence);

        nestedSequenceItemA = new DicomObject();
        nestedSequenceItemA.putValueEqualCheck(DetectorID, "test123");
        nestedSequenceA = new DicomSequence(nestedSequenceItemA);

        nestedSequenceItemB = new DicomObject();
        nestedSequenceItemB.putSequenceCheck(InstitutionalDepartmentName, nestedSequenceA);
        nestedSequenceB = new DicomSequence(nestedSequenceItemB);

        root.putSequenceCheck(ReferencedSeriesSequence, nestedSequenceB);

        root.markChildren();
    }

    @Test
    public void scopingTest() {
        final DicomValue detectorIdCheck = nestedSequenceItemA.getValueByHexCode(DetectorID);
        final List<DicomScopable> scope = detectorIdCheck.getFullScope();
        final DicomTag referencedSeriesSeq = root.getTagByHexCode(ReferencedSeriesSequence);
        final DicomTag departmentNameTag = nestedSequenceItemB.getTagByHexCode(InstitutionalDepartmentName);
        final DicomTag detectorIdTag = nestedSequenceItemA.getTagByHexCode(DetectorID);

        assertEquals(Arrays.asList(root, referencedSeriesSeq, nestedSequenceB, nestedSequenceB.getItem(0), departmentNameTag, nestedSequenceA, nestedSequenceA.getItem(0), detectorIdTag, detectorIdCheck), scope);
    }

    @Test
    public void testTagWildcardResolution() {
        final List<String> allTagsMatching0008103X = Arrays.asList(
                "(0008,1030)",
                "(0008,1031)",
                "(0008,1032)",
                "(0008,1033)",
                "(0008,1034)",
                "(0008,1035)",
                "(0008,1036)",
                "(0008,1037)",
                "(0008,1038)",
                "(0008,1039)",
                "(0008,103a)",
                "(0008,103b)",
                "(0008,103c)",
                "(0008,103d)",
                "(0008,103e)",
                "(0008,103f)"
        );
        
        assertEquals(allTagsMatching0008103X, DicomEditUtils.resolveAllDicomEditTags("(0008,103X)"));
        assertEquals(allTagsMatching0008103X, DicomEditUtils.resolveAllDicomEditTags("(0008,103x)"));
        assertEquals(Arrays.asList(
                "(0001,0000)", "(0003,0000)", "(0005,0000)", "(0007,0000)", "(0009,0000)", "(000b,0000)", "(000d,0000)", "(000f,0000)",
                "(0001,2000)", "(0003,2000)", "(0005,2000)", "(0007,2000)", "(0009,2000)", "(000b,2000)", "(000d,2000)", "(000f,2000)",
                "(0001,4000)", "(0003,4000)", "(0005,4000)", "(0007,4000)", "(0009,4000)", "(000b,4000)", "(000d,4000)", "(000f,4000)",
                "(0001,6000)", "(0003,6000)", "(0005,6000)", "(0007,6000)", "(0009,6000)", "(000b,6000)", "(000d,6000)", "(000f,6000)",
                "(0001,8000)", "(0003,8000)", "(0005,8000)", "(0007,8000)", "(0009,8000)", "(000b,8000)", "(000d,8000)", "(000f,8000)",
                "(0001,a000)", "(0003,a000)", "(0005,a000)", "(0007,a000)", "(0009,a000)", "(000b,a000)", "(000d,a000)", "(000f,a000)",
                "(0001,c000)", "(0003,c000)", "(0005,c000)", "(0007,c000)", "(0009,c000)", "(000b,c000)", "(000d,c000)", "(000f,c000)",
                "(0001,e000)", "(0003,e000)", "(0005,e000)", "(0007,e000)", "(0009,e000)", "(000b,e000)", "(000d,e000)", "(000f,e000)"
        ), DicomEditUtils.resolveAllDicomEditTags("(000#,@000)"));
    }

}
