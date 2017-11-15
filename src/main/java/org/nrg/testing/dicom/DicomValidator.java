package org.nrg.testing.dicom;

import org.nrg.testing.dicom.values.DicomSequence;

import java.io.File;
import java.util.Map;

public abstract class DicomValidator {

    public void validate(Map<File, DicomObject> fileMap, InterfileDicomValidation... interFileChecks) {
        for (Map.Entry<File, DicomObject> entry : fileMap.entrySet()) {
            validate(entry.getKey(), entry.getValue());
        }
        for (InterfileDicomValidation interfileCheck : interFileChecks) {
            interfileCheck.validate(fileMap);
        }
    }

    public abstract void validate(File actualFile, DicomObject expectedDicomObject);

    public abstract void checkTagPresent(DicomTag tag);

    public abstract void checkTagNotPresent(DicomTag tag);

    public abstract void checkTagHasValue(DicomTag tag, String value);

    public abstract void checkTagDoesntHaveValue(DicomTag tag, String value);

    public abstract void checkTagStartsWith(DicomTag tag, String value);

    public abstract void validateSequence(DicomSequence sequence);

}
