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
        subsetMatchingIntegerList(Tag.InstanceNumber, instanceNumbers)
    }

    static Function<List<Attributes>, List<Attributes>> subsetWithSeriesNumber(List<Integer> seriesNumbers) {
        subsetMatchingIntegerList(Tag.SeriesNumber, seriesNumbers)
    }

    static Function<List<Attributes>, List<Attributes>> subsetWithSeriesAndInstanceNumbers(List<Integer> seriesNumbers, List<Integer> instanceNumbers) {
        subsetWithSeriesNumber(seriesNumbers).compose(subsetWithInstanceNumber(instanceNumbers))
    }

    static Function<List<Attributes>, List<Attributes>> subsetWithSeriesAndInstanceNumbers(int seriesNumber, int instanceNumber) {
        subsetWithSeriesAndInstanceNumbers([seriesNumber], [instanceNumber])
    }

    private static Function<List<Attributes>, List<Attributes>> subsetMatchingIntegerList(int tag, List<Integer> possibleNumbers) {
        (List<Attributes> listOfDicom) -> {
            final List<String> acceptableNumbersAsStrings = possibleNumbers.collect { intVal ->
                String.valueOf(intVal)
            }
            listOfDicom.findAll { dicom ->
                dicom.getString(tag) in acceptableNumbersAsStrings // reading as String because we don't want false matches from the defaultValue on getInt
            }
        }
    }

}
