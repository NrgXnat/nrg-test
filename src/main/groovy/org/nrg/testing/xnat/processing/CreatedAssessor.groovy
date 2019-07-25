package org.nrg.testing.xnat.processing

import org.nrg.xnat.pogo.DataType

class CreatedAssessor implements ProcessingCheckable {

    private DataType dataType

    CreatedAssessor(DataType assessor) {
        dataType = assessor
    }

    String getName() {
        dataType.singularName
    }

}
