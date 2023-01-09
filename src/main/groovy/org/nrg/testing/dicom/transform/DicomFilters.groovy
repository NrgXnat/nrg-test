package org.nrg.testing.dicom.transform

import org.dcm4che3.data.DatasetWithFMI
import org.dcm4che3.data.Tag

import java.util.function.Function

class DicomFilters {

    public static final Function<List<DatasetWithFMI>, List<DatasetWithFMI>> ONLY_ONE_FILE = { List<DatasetWithFMI> listOfDicom ->
        [listOfDicom[0]]
    }

    static Function<List<DatasetWithFMI>, List<DatasetWithFMI>> subsetWithInstanceNumber(int integer) {
        subsetWithInstanceNumber([integer])
    }

    static Function<List<DatasetWithFMI>, List<DatasetWithFMI>> subsetWithInstanceNumber(List<Integer> instanceNumbers) {
        (List<DatasetWithFMI> listOfDicom) -> {
            final List<String> acceptableNumbersAsStrings = instanceNumbers.collect { intVal ->
                String.valueOf(intVal)
            }
            listOfDicom.findAll { dicom ->
                dicom.dataset.getString(Tag.InstanceNumber) in acceptableNumbersAsStrings // reading as String because we don't want false matches from the defaultValue on getInt
            }
        }
    }

}
