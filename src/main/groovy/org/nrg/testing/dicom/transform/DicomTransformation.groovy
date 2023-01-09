package org.nrg.testing.dicom.transform

import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import org.dcm4che3.data.DatasetWithFMI
import java.util.function.Function

@Builder(builderStrategy = SimpleStrategy, prefix = '')
class DicomTransformation {

    String identifier
    boolean produceZip = false
    int transformationCount = 1
    Function<List<DatasetWithFMI>, List<DatasetWithFMI>> prefilter
    TransformFunction transformFunction = TransformFunction.IDENTITY
    DicomFileWriter dicomFileWriter = new DefaultDicomWriter()

    DicomTransformation(String identifier) {
        this.identifier = identifier
    }

    DicomTransformation produceZip() {
        produceZip(true)
    }

}
