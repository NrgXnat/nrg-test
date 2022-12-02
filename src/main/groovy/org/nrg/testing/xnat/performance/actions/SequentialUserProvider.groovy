package org.nrg.testing.xnat.performance.actions

import org.nrg.xnat.pogo.users.User

class SequentialUserProvider implements PerformanceUserProvider {

    List<User> users

    SequentialUserProvider(List<User> users) {
        this.users = users
    }

    @Override
    User nextUser() {
        users.remove(0)
    }

}
