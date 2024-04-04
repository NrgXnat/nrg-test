package org.nrg.testing.xnat.performance.actions

import org.nrg.testing.xnat.Users
import org.nrg.testing.xnat.performance.PerformanceStateHelper
import org.nrg.xnat.pogo.users.User

class SequentialUserProvider implements PerformanceUserProvider {

    int numUsers = 0
    List<User> users

    SequentialUserProvider(List<User> users) {
        this.users = users
    }

    SequentialUserProvider(int numUsers) {
        this.numUsers = numUsers
    }

    @Override
    User nextUser() {
        users.remove(0)
    }

    @Override
    void setup(PerformanceStateHelper stateHelper) {
        if (numUsers > 0) {
            users = (0 ..< numUsers).collect {
                final User user = Users.genericAccount()
                stateHelper.mainAdminInterface().createUser(user)
                user
            }
        }
    }
}
