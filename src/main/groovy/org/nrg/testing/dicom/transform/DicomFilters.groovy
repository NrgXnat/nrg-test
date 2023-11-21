package org.nrg.testing.dicom.transform

import org.dcm4che3.data.Attributes
import org.dcm4che3.data.Tag

import java.util.function.Function

class DicomFilters {

    public static final Function<List<Attributes>, List<Attributes>> ONLY_ONE_FILE = { List<Attributes> listOfDicom ->
        [listOfDicom[0]]
    }

    static Function<List<Attributes>, List<Attributes>> subsetWithInstanceNumber(int integer) {
        subsetWithInstanceNumber([integer])
    }

    static Function<List<Attributes>, List<Attributes>> subsetWithInstanceNumber(List<Integer> instanceNumbers) {
        (List<Attributes> listOfDicom) -> {
            final List<String> acceptableNumbersAsStrings = instanceNumbers.collect { intVal ->
                String.valueOf(intVal)
            }
            listOfDicom.findAll { dicom ->
                dicom.getString(Tag.InstanceNumber) in acceptableNumbersAsStrings // reading as String because we don't want false matches from the defaultValue on getInt
            }
        }
    }

}
