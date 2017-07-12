package org.nrg.testing.xnat.processing;

public class CreatedAssessor implements ProcessingCheckable {

    private String name;

    public CreatedAssessor(String assessor) {
        name = assessor;
    }

    public String getName() {
        return name;
    }

}
