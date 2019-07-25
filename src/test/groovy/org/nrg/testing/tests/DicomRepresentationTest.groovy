package org.nrg.testing.tests

import org.nrg.testing.FileIOUtils
import org.nrg.testing.dicom.*
import org.nrg.testing.dicom.values.DicomSequence
import org.nrg.testing.dicom.values.DicomValue
import org.testng.annotations.Test

import static org.dcm4che3.data.Tag.*
import static org.dcm4che3.data.UID.*
import static org.testng.AssertJUnit.assertEquals

class DicomRepresentationTest {

    private final DicomObject root
    private final DicomObject referencedInstanceSequenceItem0 // 3 levels of sequences deep
    private final DicomObject referencedInstanceSequenceItem1 // 3 levels of sequences deep
    private final DicomSequence referencedInstanceSequence
    private final DicomObject referencedSeriesSequenceItem // 2 levels of sequences deep
    private final DicomSequence referencedSeriesSequence
    private final DicomObject referencedStudySequenceItem // 1 level of sequences deep
    private final DicomSequence referencedStudySequence

    /**
     * The structure of the DICOM dataset is taken from PS3.3 10.37 Related Information Entities Macro. Technically the attributes in this module are currently all embedded even further
     * in sequences, but I'll just pretend they're at the root level.
     */
    DicomRepresentationTest() {
        root = new RootDicomObject() // going to build this in reverse
        root.putExistenceChecks(AccessionNumber) // just checking no exception thrown
        root.putNonexistenceChecks(ReferencedSOPInstanceUID) // just checking no exception thrown

        referencedInstanceSequenceItem0 = new DicomObject()
        referencedInstanceSequenceItem1 = new DicomObject()
        referencedInstanceSequenceItem0.putValueEqualCheck(ReferencedSOPClassUID, MRImageStorage)
        referencedInstanceSequenceItem0.putValueEqualCheck(ReferencedSOPInstanceUID, '1.2.3.4.5.6.7.8.1000')
        referencedInstanceSequenceItem1.putValueEqualCheck(ReferencedSOPClassUID, MRImageStorage)
        referencedInstanceSequenceItem1.putValueEqualCheck(ReferencedSOPInstanceUID, '1.2.3.4.5.6.7.8.2000')
        referencedInstanceSequence = new DicomSequence(referencedInstanceSequenceItem0, referencedInstanceSequenceItem1)

        referencedSeriesSequenceItem = new DicomObject()
        referencedSeriesSequenceItem.putValueEqualCheck(SeriesInstanceUID, '1.2.3.4.5.6.7.8')
        referencedSeriesSequenceItem.putSequenceCheck(ReferencedInstanceSequence, referencedInstanceSequence)
        referencedSeriesSequence = new DicomSequence(referencedSeriesSequenceItem)

        referencedStudySequenceItem = new DicomObject()
        referencedStudySequenceItem.putValueEqualCheck(StudyInstanceUID, '1.2.3.4.5.6')
        referencedStudySequenceItem.putSequenceCheck(ReferencedSeriesSequence, referencedSeriesSequence)
        referencedStudySequence = new DicomSequence(referencedStudySequenceItem)

        root.putSequenceCheck(ReferencedStudySequence, referencedStudySequence)
        root.markChildren()
    }

    @Test
    void scopingTest() {
        final DicomTag tagForReferencedStudySequence = root.getTagByHexCode(ReferencedStudySequence)
        final DicomTag tagForReferencedSeriesSequence = referencedStudySequenceItem.getTagByHexCode(ReferencedSeriesSequence)
        final DicomTag tagForReferencedInstanceSequence = referencedSeriesSequenceItem.getTagByHexCode(ReferencedInstanceSequence)
        final DicomTag tagForItem1ReferencedSopInstanceUID = referencedInstanceSequenceItem1.getTagByHexCode(ReferencedSOPInstanceUID)
        final DicomValue referencedSopInstance1Check = referencedInstanceSequenceItem1.getValueByHexCode(ReferencedSOPInstanceUID)
        final List<DicomScopable> fullScope = referencedSopInstance1Check.fullScope

        assertEquals([
                root,
                tagForReferencedStudySequence,
                referencedStudySequence,
                referencedStudySequence.items[0],
                tagForReferencedSeriesSequence,
                referencedSeriesSequence,
                referencedSeriesSequence.items[0],
                tagForReferencedInstanceSequence,
                referencedInstanceSequence,
                referencedInstanceSequence.items[1],
                tagForItem1ReferencedSopInstanceUID,
                referencedSopInstance1Check
        ], fullScope)
    }

    @Test
    void testTagWildcardResolution() {
        final List<String> allTagsMatching0008103X = [
                '(0008,1030)',
                '(0008,1031)',
                '(0008,1032)',
                '(0008,1033)',
                '(0008,1034)',
                '(0008,1035)',
                '(0008,1036)',
                '(0008,1037)',
                '(0008,1038)',
                '(0008,1039)',
                '(0008,103a)',
                '(0008,103b)',
                '(0008,103c)',
                '(0008,103d)',
                '(0008,103e)',
                '(0008,103f)'
        ]

        assertEquals(allTagsMatching0008103X, DicomEditUtils.resolveAllDicomEditTags('(0008,103X)'))
        assertEquals(allTagsMatching0008103X, DicomEditUtils.resolveAllDicomEditTags('(0008,103x)'))
        assertEquals([
                '(0001,0000)', '(0003,0000)', '(0005,0000)', '(0007,0000)', '(0009,0000)', '(000b,0000)', '(000d,0000)', '(000f,0000)',
                '(0001,2000)', '(0003,2000)', '(0005,2000)', '(0007,2000)', '(0009,2000)', '(000b,2000)', '(000d,2000)', '(000f,2000)',
                '(0001,4000)', '(0003,4000)', '(0005,4000)', '(0007,4000)', '(0009,4000)', '(000b,4000)', '(000d,4000)', '(000f,4000)',
                '(0001,6000)', '(0003,6000)', '(0005,6000)', '(0007,6000)', '(0009,6000)', '(000b,6000)', '(000d,6000)', '(000f,6000)',
                '(0001,8000)', '(0003,8000)', '(0005,8000)', '(0007,8000)', '(0009,8000)', '(000b,8000)', '(000d,8000)', '(000f,8000)',
                '(0001,a000)', '(0003,a000)', '(0005,a000)', '(0007,a000)', '(0009,a000)', '(000b,a000)', '(000d,a000)', '(000f,a000)',
                '(0001,c000)', '(0003,c000)', '(0005,c000)', '(0007,c000)', '(0009,c000)', '(000b,c000)', '(000d,c000)', '(000f,c000)',
                '(0001,e000)', '(0003,e000)', '(0005,e000)', '(0007,e000)', '(0009,e000)', '(000b,e000)', '(000d,e000)', '(000f,e000)'
        ], DicomEditUtils.resolveAllDicomEditTags('(000#,@000)'))
    }

    @Test
    void testFullScopeStringRepresentation() {
        assertEquals(FileIOUtils.loadResource('sample_dicom_representation.txt').text, referencedInstanceSequenceItem1.getTagByHexCode(ReferencedSOPInstanceUID).fullScopeStringRepresentation)
    }

}
