package org.nrg.testing.dicom

import org.nrg.testing.DicomUtils

import static org.testng.AssertJUnit.assertEquals

class DistinctTagValues implements InterfileDicomValidation {

    private int dicomTag

    DistinctTagValues(int dicomTag) {
        this.dicomTag = dicomTag
    }

    @Override
    void validate(Map<File, DicomObject> dicomObjectFileMap) {
        final List<String> distinctValues = dicomObjectFileMap.keySet().collect { dicomFile ->
            DicomUtils.readDicom(dicomFile).getStrings(dicomTag).join(DicomUtils.DELIMITER)
        }.unique()
        final int numFiles = dicomObjectFileMap.keySet().size()
        assertEquals(
                "All DICOM files were expected to have distinct values for tag ${DicomUtils.intToFullHexString(dicomTag)}. Instead, the following values were found: ${distinctValues} for ${numFiles} files.",
                numFiles,
                distinctValues.size()
        )
    }

}
