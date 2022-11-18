package org.nrg.testing.dicom.transform

import org.dcm4che3.data.DatasetWithFMI

import java.util.function.Function

class DicomFilters {

    public static final Function<List<DatasetWithFMI>, List<DatasetWithFMI>> ONLY_ONE_FILE = { List<DatasetWithFMI> listOfDicom ->
        [listOfDicom[0]]
    }

}
