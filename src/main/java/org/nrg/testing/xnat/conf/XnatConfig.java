package org.nrg.testing.xnat.conf;

import com.jayway.restassured.specification.RequestSpecification;
import org.nrg.testing.xnat.Users;
import org.nrg.xnat.pogo.users.User;
import org.nrg.xnat.rest.Credentials;

public class XnatConfig {

    private String mainUsername;
    private String mainPassword;
    private String mainAdminUsername;
    private String mainAdminPassword;
    private String adminUsername;
    private String adminPassword;
    private String xnatVersion;
    private User mainUser;
    private User mainAdminUser;
    private User adminUser;
    private String xnatUrl;
    private boolean init = true;
    
    public XnatConfig mainUsername(String seleniumUsername) {
        this.mainUsername = seleniumUsername;
        return this;
    }

    public XnatConfig mainPassword(String seleniumPassword) {
        this.mainPassword = seleniumPassword;
        return this;
    }

    public XnatConfig mainAdminUsername(String seleniumAdminUsername) {
        this.mainAdminUsername = seleniumAdminUsername;
        return this;
    }

    public XnatConfig mainAdminPassword(String seleniumAdminPassword) {
        this.mainAdminPassword = seleniumAdminPassword;
        return this;
    }

    public XnatConfig adminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
        return this;
    }

    public XnatConfig adminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
        return this;
    }

    public XnatConfig xnatVersion(String xnatVersion) {
        this.xnatVersion = xnatVersion;
        return this;
    }

    public XnatConfig xnatUrl(String xnatUrl) {
        this.xnatUrl = xnatUrl;
        return this;
    }

    public XnatConfig init(boolean init) {
        this.init = init;
        return this;
    }

    public XnatConfig build() {
        mainUser = Users.constructMainAccount(mainUsername, mainPassword);
        mainAdminUser = Users.constructMainAdminAccount(mainAdminUsername, mainAdminPassword);
        adminUser = new User(adminUsername).password(adminPassword).admin(true);
        return this;
    }

    public String getMainUsername() {
        return mainUsername;
    }

    public String getMainPassword() {
        return mainPassword;
    }

    public String getMainAdminUsername() {
        return mainAdminUsername;
    }

    public String getMainAdminPassword() {
        return mainAdminPassword;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public String getXnatVersion() {
        return xnatVersion;
    }

    public User getMainUser() {
        return mainUser;
    }

    public User getMainAdminUser() {
        return mainAdminUser;
    }

    public User getAdminUser() {
        return adminUser;
    }

    public String getXnatUrl() {
        return xnatUrl;
    }

    public boolean getInitSetting() {
        return init;
    }

    public RequestSpecification getMainCredentials() {
        return Credentials.build(mainUser);
    }

    public RequestSpecification getMainAdminCredentials() {
        return Credentials.build(mainAdminUser);
    }

    public RequestSpecification getAdminCredentials() {
        return Credentials.build(adminUser);
    }

    public static XnatConfig buildDefaultConfig() {
        return new XnatConfig().
                mainUsername(Settings.MAIN_USERNAME).
                mainPassword(Settings.MAIN_PASS).
                mainAdminUsername(Settings.MAIN_ADMIN_USERNAME).
                mainAdminPassword(Settings.MAIN_ADMIN_PASS).
                adminUsername(Settings.ADMIN_USERNAME).
                adminPassword(Settings.ADMIN_PASS).
                xnatVersion(Settings.XNAT_VERSION).
                xnatUrl(Settings.BASEURL).
                init(Settings.INIT_SETTING).
                build();
    }
    
}
