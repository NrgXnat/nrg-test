package org.nrg.testing.xnat.components

import org.nrg.testing.xnat.BaseXnatRestTest
import org.nrg.xnat.pogo.AnonScript
import org.nrg.xnat.pogo.Project

class SiteAnon extends AnonStep<SiteAnon> {

    SiteAnon(String contents) {
        super(contents)
    }

    SiteAnon() {
        super()
    }

    @Override
    void perform(BaseXnatRestTest xnatRestTest, Project project) {
        xnatRestTest.mainAdminInterface().enableSiteAnonScript()
        xnatRestTest.mainAdminInterface().setSiteAnonScript(new AnonScript().contents(contents))
    }

}
