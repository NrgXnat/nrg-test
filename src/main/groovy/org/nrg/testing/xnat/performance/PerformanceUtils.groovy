package org.nrg.testing.xnat.performance

import org.nrg.testing.FileIOUtils
import org.nrg.testing.xnat.conf.Settings
import org.nrg.testing.xnat.performance.persistence.CheckablePerformanceEntry
import org.nrg.testing.xnat.performance.persistence.HistoricalPerformanceCatalog
import org.nrg.xnat.interfaces.XnatInterface

import java.nio.file.Path
import java.nio.file.Paths

class PerformanceUtils {

    public static final String DIR = 'performance'
    public static final Path PERFORMANCE_SUBDIR = init()

    static <X extends CheckablePerformanceEntry<X>> HistoricalPerformanceCatalog<X> readHistory(String identifier) {
        final File historicalRecordFile = recordFor(identifier)
        historicalRecordFile.exists() ?
                XnatInterface.XNAT_REST_MAPPER.readValue(historicalRecordFile, HistoricalPerformanceCatalog<X>) as HistoricalPerformanceCatalog<X> :
                new HistoricalPerformanceCatalog<X>()
    }

    static File recordFor(String identifier) {
        PERFORMANCE_SUBDIR.resolve("${identifier}.json").toFile()
    }

    static File chartFor(String identifier) {
        PERFORMANCE_SUBDIR.resolve("${identifier}.tex").toFile()
    }

    static <X extends CheckablePerformanceEntry<X>> void writeHistory(String identifier, HistoricalPerformanceCatalog<X> historicalRecord) {
        XnatInterface.XNAT_REST_MAPPER
                .writerWithDefaultPrettyPrinter()
                .writeValue(recordFor(identifier), historicalRecord)
    }

    private static Path init() {
        final Path path = Paths.get(Settings.DATA_LOCATION, DIR)
        FileIOUtils.mkdirs(path)
        path
    }

}
