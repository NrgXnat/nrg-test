package org.nrg.testing.xnat.components

import org.nrg.testing.xnat.BaseXnatRestTest
import org.nrg.xnat.pogo.AnonScript
import org.nrg.xnat.pogo.Project

class ProjectAnon extends AnonStep<ProjectAnon> {

    ProjectAnon(String contents) {
        super(contents)
    }

    ProjectAnon() {
        super()
    }

    @Override
    void perform(BaseXnatRestTest xnatRestTest, Project project) {
        xnatRestTest.mainInterface().setProjectAnonScript(project, new AnonScript().contents(contents))
        xnatRestTest.mainInterface().enableProjectAnonScript(project)
    }

}
