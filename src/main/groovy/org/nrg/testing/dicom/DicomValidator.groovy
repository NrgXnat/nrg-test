package org.nrg.testing.dicom

import org.nrg.testing.dicom.values.DicomSequence

abstract class DicomValidator {

    void validate(Map<File, DicomObject> fileMap, InterfileDicomValidation... interFileChecks) {
        fileMap.each { file, dicomObject ->
            validate(file, dicomObject)
        }
        interFileChecks.each { interFileCheck ->
            interFileCheck.validate(fileMap)
        }
    }

    abstract void validate(File actualFile, DicomObject expectedDicomObject)

    abstract void checkTagPresent(DicomTag tag)

    abstract void checkTagNotPresent(DicomTag tag)

    abstract void checkTagHasValue(DicomTag tag, String value)

    abstract void checkTagDoesntHaveValue(DicomTag tag, String value)

    abstract void checkTagStartsWith(DicomTag tag, String value)

    abstract void validateSequence(DicomSequence sequence)

}
