package org.nrg.testing.xnat.components

import org.nrg.testing.xnat.BaseXnatRestTest
import org.nrg.xnat.pogo.Project

interface TestComponent {

    void perform(BaseXnatRestTest xnatRestTest, Project project)

}