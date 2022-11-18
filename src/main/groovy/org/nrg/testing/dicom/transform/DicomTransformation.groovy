package org.nrg.testing.dicom.transform

import org.dcm4che3.data.DatasetWithFMI
import java.util.function.Function

class DicomTransformation {

    String identifier
    boolean produceZip = false
    int transformationCount = 1
    Function<List<DatasetWithFMI>, List<DatasetWithFMI>> prefilter
    TransformFunction transformFunction

    DicomTransformation(String identifier) {
        this.identifier = identifier
    }

    DicomTransformation produceZip() {
        produceZip = true
        this
    }

    DicomTransformation transformationCount(int count) {
        transformationCount = count
        this
    }

    DicomTransformation prefilter(Function<List<DatasetWithFMI>, List<DatasetWithFMI>> prefilter) {
        this.prefilter = prefilter
        this
    }

    DicomTransformation transformFunction(TransformFunction transformFunction) {
        this.transformFunction = transformFunction
        this
    }

}
