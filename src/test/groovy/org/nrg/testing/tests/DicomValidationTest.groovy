package org.nrg.testing.tests

import org.dcm4che3.data.VR
import org.nrg.testing.UnitTestUtils
import org.nrg.testing.dicom.DicomFileValidator
import org.nrg.testing.dicom.DicomObject
import org.nrg.testing.dicom.DicomValidator
import org.nrg.testing.dicom.RootDicomObject
import org.nrg.testing.dicom.values.DicomSequence
import org.testng.annotations.Test

import java.nio.file.Paths

import static org.dcm4che3.data.Tag.*
import static org.testng.AssertJUnit.assertEquals
import static org.testng.AssertJUnit.fail

class DicomValidationTest {

    private final File sampleFile = Paths.get(UnitTestUtils.DATA_LOCATION, 'sample1_single_file.dcm').toFile()
    private final DicomValidator validator = new DicomFileValidator()

    @Test
    void testValidation() {
        final DicomObject dicomObject = new RootDicomObject()
        dicomObject.putValueEqualCheck(ImplementationVersionName, 'dcm4che-2.0')
        dicomObject.putValueEqualCheck(SourceApplicationEntityTitle, 'DicomBrowser')
        dicomObject.putValueEqualCheck(Modality, 'MR')
        dicomObject.putValueEqualCheck(IdentifyingComments, 'http://nrg.wustl.edu/projects/DICOM/sample1.zip')
        dicomObject.putValueNotEqualCheck(Manufacturer, 'FUJI')
        dicomObject.putExistenceChecks(AccessionNumber, PerformingPhysicianName)
        dicomObject.putNonexistenceChecks(OtherPatientIDs)
        dicomObject.putWildcardedNonexistenceCheck('(0008,30XX)')
        dicomObject.putWildcardedNonexistenceCheck('(0008,103#)')

        final DicomObject sequenceItem0 = new DicomObject()
        sequenceItem0.putValueEqualCheck(ReferencedSOPClassUID, org.dcm4che3.data.UID.MRImageStorage)
        sequenceItem0.putValueNotEqualCheck(ReferencedSOPInstanceUID, org.dcm4che3.data.UID.MRImageStorage)

        final DicomObject sequenceItem1 = new DicomObject()
        sequenceItem1.putExistenceChecks(ReferencedSOPClassUID)
        sequenceItem1.putNonexistenceChecks(CodeValue)

        final DicomSequence sequence = new DicomSequence(sequenceItem0, sequenceItem1).disableSizeCheck()
        dicomObject.putSequenceCheck(ReferencedImageSequence, sequence)
        validator.validate(sampleFile, dicomObject)

        assertEquals(0, sequence.getItem(0).getSequenceIndex())
        assertEquals(1, sequence.getItem(1).getSequenceIndex())
    }

    @Test
    void testBadCheckInRootObject() {
        final DicomObject dicomObject = new RootDicomObject()
        dicomObject.putValueEqualCheck(Modality, 'CT')
        expectValidationFailure(dicomObject)
    }

    @Test
    void testBadCheckInSequence() {
        final DicomObject dicomObject = new RootDicomObject()
        final DicomObject sequenceItem0 = new DicomObject()
        sequenceItem0.putValueEqualCheck(ReferencedSOPClassUID, '1.2.840.10008.5.1.4.1.1.4000')
        dicomObject.putSequenceCheck(ReferencedImageSequence, new DicomSequence(sequenceItem0, sequenceItem0))
        expectValidationFailure(dicomObject)
    }

    @Test
    void testBadCheckInSequenceItemAddedAfterCheck() {
        final DicomObject dicomObject = new RootDicomObject()
        final DicomObject sequenceItem0 = new DicomObject()
        dicomObject.putSequenceCheck(ReferencedImageSequence, new DicomSequence(sequenceItem0, sequenceItem0))
        sequenceItem0.putValueEqualCheck(ReferencedSOPClassUID, '1.2.840.10008.5.1.4.1.1.4000')
        expectValidationFailure(dicomObject)
    }

    @Test
    void testBadWildcardedCheck() {
        final DicomObject dicomObject = new RootDicomObject()
        dicomObject.putWildcardedNonexistenceCheck('(0008,103@)')
        expectValidationFailure(dicomObject)
    }

    @Test
    void testBadSequenceSizeCheck() {
        final DicomObject dicomObject = new RootDicomObject()
        final DicomObject sequenceItem0 = new DicomObject()
        sequenceItem0.putValueEqualCheck(ReferencedSOPClassUID, org.dcm4che3.data.UID.MRImageStorage)
        sequenceItem0.putValueNotEqualCheck(ReferencedSOPInstanceUID, org.dcm4che3.data.UID.MRImageStorage)
        dicomObject.putSequenceCheck(ReferencedImageSequence, new DicomSequence(sequenceItem0, sequenceItem0))
        expectValidationFailure(dicomObject)
    }

    @Test
    void testSequenceSizeCheckDisabled() {
        final DicomObject dicomObject = new RootDicomObject()
        final DicomObject sequenceItem0 = new DicomObject()
        sequenceItem0.putValueEqualCheck(ReferencedSOPClassUID, org.dcm4che3.data.UID.MRImageStorage)
        sequenceItem0.putValueNotEqualCheck(ReferencedSOPInstanceUID, org.dcm4che3.data.UID.MRImageStorage)
        dicomObject.putSequenceCheck(ReferencedImageSequence, new DicomSequence(sequenceItem0).disableSizeCheck())
        validator.validate(sampleFile, dicomObject)
    }

    @Test
    void testBadVrCheck() {
        final DicomObject dicomObject = new RootDicomObject()
        dicomObject.putValueEqualCheck(SOPClassUID, org.dcm4che3.data.UID.MRImageStorage, VR.CS)
        expectValidationFailure(dicomObject)
    }

    @Test
    void testVrCheck() {
        final DicomObject dicomObject = new RootDicomObject()
        dicomObject.putValueEqualCheck(SOPClassUID, org.dcm4che3.data.UID.MRImageStorage, VR.UI)
        validator.validate(sampleFile, dicomObject)
    }

    @Test
    void testBadValuesEqualCheck() {
        final DicomObject dicomObject = new RootDicomObject()
        dicomObject.putValueEqualCheck("(0018,0093)", dicomObject.getTagElement("(0018,0095"))
        expectValidationFailure(dicomObject)
    }

    @Test
    void testValuesEqualCheck() {
        final DicomObject dicomObject = new RootDicomObject()
        dicomObject.putValueEqualCheck("(0018,0093)", dicomObject.getTagElement("(0018,0094)"))
        validator.validate(sampleFile, dicomObject)
    }

    private void expectValidationFailure(DicomObject dicomObject) {
        try {
            validator.validate(sampleFile, dicomObject)
        } catch (Exception | Error ignored) {
            return
        }
        fail('DICOM validation should have thrown an exception.')
    }

}
