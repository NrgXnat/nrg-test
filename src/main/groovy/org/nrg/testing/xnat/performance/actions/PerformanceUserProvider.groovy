package org.nrg.testing.xnat.performance.actions

import org.nrg.xnat.pogo.users.User

interface PerformanceUserProvider {

    User nextUser()

}