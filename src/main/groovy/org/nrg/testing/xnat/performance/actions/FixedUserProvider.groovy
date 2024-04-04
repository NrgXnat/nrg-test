package org.nrg.testing.xnat.performance.actions

import org.nrg.testing.xnat.performance.PerformanceStateHelper
import org.nrg.xnat.pogo.users.User

class FixedUserProvider implements PerformanceUserProvider {

    User user

    FixedUserProvider(User user) {
        this.user = user
    }

    @Override
    User nextUser() {
        user
    }

    @Override
    void setup(PerformanceStateHelper stateHelper) {

    }

}
