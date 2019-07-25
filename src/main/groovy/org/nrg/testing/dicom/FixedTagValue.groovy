package org.nrg.testing.dicom

import org.nrg.testing.DicomUtils

import static org.testng.AssertJUnit.assertEquals

class FixedTagValue implements InterfileDicomValidation {

    private int dicomTag

    FixedTagValue(int dicomTag) {
        this.dicomTag = dicomTag
    }

    @Override
    void validate(Map<File, DicomObject> dicomObjectFileMap) {
        final List<String> distinctValues = dicomObjectFileMap.keySet().collect { dicomFile ->
            DicomUtils.readDicom(dicomFile).dataset.getStrings(dicomTag).join(DicomUtils.DELIMITER)
        }.unique()
        assertEquals("All DICOM files were expected to have the same value for tag ${DicomUtils.intToFullHexString(dicomTag)}. Instead, the following values were found: ${distinctValues}.", 1, distinctValues.size())
    }

}
