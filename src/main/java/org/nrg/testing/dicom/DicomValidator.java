package org.nrg.testing.dicom;

import org.nrg.testing.dicom.values.DicomSequence;

public interface DicomValidator {

    void checkTagPresent(DicomTag tag);

    void checkTagNotPresent(DicomTag tag);

    void checkTagPresentSomewhere(DicomTag tag);

    void checkTagPresentNowhere(DicomTag tag);

    void checkTagHasValue(DicomTag tag, String value);

    void checkTagDoesntHaveValue(DicomTag tag, String value);

    void validateSequence(DicomSequence sequence);

}
