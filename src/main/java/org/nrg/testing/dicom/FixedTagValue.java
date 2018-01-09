package org.nrg.testing.dicom;

import org.nrg.testing.file.FileIO;

import java.io.File;
import java.util.Map;

import static org.testng.AssertJUnit.assertEquals;

public class FixedTagValue implements InterfileDicomValidation {

    private int dicomTag;

    public FixedTagValue(int dicomTag) {
        this.dicomTag = dicomTag;
    }

    @Override
    public void validate(Map<File, DicomObject> dicomObjectFileMap) {
        String headerValue = null;

        for (Map.Entry<File, DicomObject> fileEntry : dicomObjectFileMap.entrySet()) {
            String currentFileHeaderValue = FileIO.readDicomFile(fileEntry.getKey()).getDataset().getString(dicomTag);
            if (headerValue == null) {
                headerValue = currentFileHeaderValue;
            } else {
                assertEquals("All DICOM files were expected to have the same value for tag 0x" + org.nrg.xnat.util.DicomUtils.intToSimpleHeaderString(dicomTag), headerValue, currentFileHeaderValue);
            }
        }
    }

}
