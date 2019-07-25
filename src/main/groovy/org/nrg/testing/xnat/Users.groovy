package org.nrg.testing.xnat

import org.nrg.testing.util.RandomHelper
import org.nrg.testing.xnat.conf.Settings
import org.nrg.xnat.pogo.users.User

class Users {

    static User genericAccount() {
        final String user = RandomHelper.randomID()
        new User(user).password(user).firstName('Test').lastName('User').email(Settings.permuteSeleniumEmail()).enabled(true).verified(true)
    }

    static User constructMainAccount(String username, String password) {
        new User(username).password(password).firstName('Test').lastName('User').email(Settings.EMAIL).enabled(true).verified(true)
    }

    static User constructMainAdminAccount(String username, String password) {
        new User(username).password(password).firstName('Admin').lastName('TestUser').email(Settings.EMAIL).admin(true).enabled(true).verified(true)
    }

}
