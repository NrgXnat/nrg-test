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

    List<String> findVersionOverlapWithOtherCatalogs(Collection<HistoricalPerformanceCatalog<X>> otherCatalogs) {
        final List<String> selfCatalogVersions = entries*.xnatVersion
        final List<List<String>> otherCatalogsVersions = otherCatalogs.entries*.xnatVersion
        selfCatalogVersions.findAll { selfVersion ->
            otherCatalogsVersions.every { individualOtherCatalog ->
                selfVersion in individualOtherCatalog
            }
        }
    }

}
