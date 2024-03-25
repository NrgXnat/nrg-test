package org.nrg.testing.xnat.performance.persistence

class HistoricalPerformanceCatalog<X extends CheckablePerformanceEntry<X>> {

    List<X> entries = []

    X findBaseline() {
        if (entries.isEmpty()) {
            return null
        }
        entries.find { entry ->
            entry.baseline
        } ?: entries.last()
    }

    X lookupEntryByVersion(String version) {
        entries.find { entry ->
            entry.xnatVersion == version
        }
    }

}
