package org.nrg.testing.xnat.components

import org.nrg.testing.xnat.XnatObjectUtils
import org.nrg.xnat.enums.DicomEditVersion

import java.nio.file.Paths

abstract class AnonStep<X extends AnonStep<X>> implements TestComponent {

    String contents

    AnonStep(String contents) {
        this.contents = contents
    }

    AnonStep() {

    }

    X fromFile(DicomEditVersion dicomEditVersion, String requiredPathComponent, String... additionalComponents) {
        contents = XnatObjectUtils.anonScriptFromFile(
                dicomEditVersion,
                Paths.get(requiredPathComponent, additionalComponents).toString()
        ).contents
        this as X
    }


    X fromFile(String requiredPathComponent, String... additionalComponents) {
        fromFile(DicomEditVersion.DE_6, requiredPathComponent, additionalComponents)
    }

}
