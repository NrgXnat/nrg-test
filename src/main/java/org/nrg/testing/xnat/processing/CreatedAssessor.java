package org.nrg.testing.xnat.processing;

import org.nrg.xnat.pogo.DataType;

public class CreatedAssessor implements ProcessingCheckable {

    private DataType dataType;

    public CreatedAssessor(DataType assessor) {
        dataType = assessor;
    }

    public String getName() {
        return dataType.getSingularName();
    }

}
