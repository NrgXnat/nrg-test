package org.nrg.testing.xnat;

import org.nrg.testing.util.RandomHelper;
import org.nrg.testing.xnat.conf.Settings;
import org.nrg.testing.xnat.rest.XnatRestDriver;
import org.nrg.xnat.pojo.User;

public class Users {

    public static User genericAccount(XnatRestDriver xnatDriver) {
        final String user = RandomHelper.randomID();
        return new User(user).password(user).firstName("Test").lastName("User").email(xnatDriver.permuteSeleniumEmail());
    }

    public static User constructMainAccount(String username, String password) {
        return new User(username).password(password).firstName("Selenium").lastName("Selenium").email(Settings.EMAIL);
    }

    public static User constructSeleniumMainAccount(String username, String password) {
        return new User(username).password(password).firstName("Selenium").lastName("Admin").email(Settings.EMAIL).admin(true);
    }

}
