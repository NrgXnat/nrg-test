package org.nrg.testing.tests

import org.dcm4che3.data.Attributes
import org.dcm4che3.data.DatasetWithFMI
import org.dcm4che3.data.Tag
import org.dcm4che3.data.UID
import org.dcm4che3.data.VR
import org.nrg.testing.dicom.transform.XnatDefaultTemplatizedNamerWriter
import org.testng.annotations.Test

import static org.testng.AssertJUnit.assertEquals

class DicomNamerTest {

    @Test
    void testSample1Naming() {
        final Attributes dataset = new Attributes()
        dataset.setString(Tag.SOPClassUID, VR.UI, UID.MRImageStorage)
        dataset.setString(Tag.SOPInstanceUID, VR.UI, '1.3.12.2.1107.5.2.32.35177.3.2006121409370560368818294')
        dataset.setString(Tag.StudyInstanceUID, VR.UI, '1.3.12.2.1107.5.2.32.35177.30000006121218324675000000034')
        dataset.setInt(Tag.SeriesNumber, VR.IS, 4)
        dataset.setInt(Tag.InstanceNumber, VR.IS, 80)
        assertEquals(
                '1.3.12.2.1107.5.2.32.35177.30000006121218324675000000034-4-80-nrjcls.dcm',
                new XnatDefaultTemplatizedNamerWriter().calculateNameFor(
                        new DatasetWithFMI(dataset.createFileMetaInformation(UID.ExplicitVRLittleEndian), dataset)
                )
        )
    }

}
