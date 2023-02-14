package org.nrg.testing.xnat.components

import org.nrg.testing.xnat.BaseXnatRestTest
import org.nrg.xnat.pogo.AnonScript
import org.nrg.xnat.pogo.Project

class ProjectAnon implements TestComponent {

    private final String contents

    ProjectAnon(String contents) {
        this.contents = contents
    }

    @Override
    void perform(BaseXnatRestTest xnatRestTest, Project project) {
        xnatRestTest.mainInterface().setProjectAnonScript(project, new AnonScript().contents(contents))
        xnatRestTest.mainInterface().enableProjectAnonScript(project)
    }

}
