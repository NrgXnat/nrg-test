package org.nrg.testing.dicom

abstract class DicomScopable {

    DicomScopable parent

    abstract void markChildren()

    List<DicomScopable> getFullScope(List<DicomScopable> partialList = null) {
        if (partialList == null) {
            getParent().getFullScope([this])
        } else {
            partialList << this
            getParent().getFullScope(partialList)
        }
    }

    String getFullScopeStringRepresentation() {
        final List<DicomScopable> scope = getFullScope()
        final List<String> representation = [(scope[1] as DicomTag).fullHexString()]
        if (scope.size() > 3) {
            (3 ..< scope.size()).step(3).each { i -> // predictable order in the scope: DicomObject -> Element -> Sequence -> SequenceItem (another DicomObject) -> Element -> Sequence -> SequenceItem ...
                final Number scaledIndex = i.intdiv(3)
                final int baseIndent = 8 * scaledIndex - 4
                final String sequenceLevelIndicators = '>' * scaledIndex
                representation << "${' ' *  baseIndent}${sequenceLevelIndicators} Sequence item #${(scope[i] as SequenceItem).sequenceIndex}".toString()
                representation << "${' ' * (baseIndent + 4)}${sequenceLevelIndicators} ${(scope[i + 1] as DicomTag).fullHexString()}".toString()
            }
        }
        representation.join('\n') + '\n'
    }

}
