package org.nrg.testing.dicom;

import java.io.File;
import java.util.Map;

public interface InterfileDicomValidation {

    void validate(Map<File, DicomObject> dicomObjectFileMap);

}
