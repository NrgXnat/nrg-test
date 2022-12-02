package org.nrg.testing.xnat.performance

import org.nrg.testing.xnat.rest.XnatRestDriver
import org.nrg.xnat.interfaces.XnatInterface
import org.nrg.xnat.pogo.users.User

class PerformanceStateHelper {

    private XnatRestDriver restDriver

    PerformanceStateHelper(XnatRestDriver restDriver) {
        this.restDriver = restDriver
    }

    XnatInterface interfaceFor(User user) {
        restDriver.interfaceFor(user ?: mainUser())
    }

    XnatInterface mainInterface() {
        interfaceFor(mainUser())
    }

    XnatInterface mainAdminInterface() {
        interfaceFor(mainAdminUser())
    }

    User mainUser() {
        restDriver.mainUser
    }

    User mainAdminUser() {
        restDriver.mainAdminUser
    }

}
