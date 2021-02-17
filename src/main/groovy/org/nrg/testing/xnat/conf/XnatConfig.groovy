package org.nrg.testing.xnat.conf

import com.jayway.restassured.specification.RequestSpecification
import org.nrg.testing.xnat.Users
import org.nrg.xnat.versions.XnatVersion
import org.nrg.xnat.pogo.users.User
import org.nrg.xnat.rest.Credentials

class XnatConfig {

    String mainUsername
    String mainPassword
    String mainAdminUsername
    String mainAdminPassword
    String adminUsername
    String adminPassword
    Class<? extends XnatVersion> xnatVersion
    User mainUser
    User mainAdminUser
    User adminUser
    String xnatUrl
    String hostName
    boolean init = true

    String getHostName() {
        new URI(xnatUrl).host
    }

    XnatConfig mainUsername(String user) {
        setMainUsername(user)
        this
    }

    XnatConfig mainPassword(String password) {
        setMainPassword(password)
        this
    }

    XnatConfig mainAdminUsername(String user) {
        setMainAdminUsername(user)
        this
    }

    XnatConfig mainAdminPassword(String password) {
        setMainAdminPassword(password)
        this
    }

    XnatConfig adminUsername(String user) {
        setAdminUsername(user)
        this
    }

    XnatConfig adminPassword(String password) {
        setAdminPassword(password)
        this
    }

    XnatConfig xnatVersion(Class<? extends XnatVersion> versionClass) {
        setXnatVersion(versionClass)
        this
    }

    XnatConfig xnatUrl(String xnatUrl) {
        setXnatUrl(xnatUrl)
        this
    }

    XnatConfig init(boolean init) {
        setInit(init)
        this
    }

    XnatConfig build() {
        mainUser = Users.constructMainAccount(mainUsername, mainPassword)
        mainAdminUser = Users.constructMainAdminAccount(mainAdminUsername, mainAdminPassword)
        adminUser = new User(adminUsername).password(adminPassword).admin(true)
        this
    }

    RequestSpecification getMainCredentials() {
        Credentials.build(mainUser)
    }

    RequestSpecification getMainAdminCredentials() {
        Credentials.build(mainAdminUser)
    }

    RequestSpecification getAdminCredentials() {
        Credentials.build(adminUser)
    }

    static XnatConfig buildDefaultConfig() {
        new XnatConfig().
                mainUsername(Settings.MAIN_USERNAME).
                mainPassword(Settings.MAIN_PASS).
                mainAdminUsername(Settings.MAIN_ADMIN_USERNAME).
                mainAdminPassword(Settings.MAIN_ADMIN_PASS).
                adminUsername(Settings.ADMIN_USERNAME).
                adminPassword(Settings.ADMIN_PASS).
                xnatVersion(Settings.XNAT_VERSION).
                xnatUrl(Settings.BASEURL).
                init(Settings.INIT_SETTING).
                build()
    }
    
}
