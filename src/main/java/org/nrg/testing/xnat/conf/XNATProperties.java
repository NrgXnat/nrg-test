/*
 * org.nrg.selenium.xnat.XNATProperties
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.testing.xnat.conf;

import org.apache.log4j.Logger;
import org.nrg.testing.CommonUtils;
import org.nrg.testing.util.BaseProperties;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class XNATProperties extends BaseProperties {

    private static final Logger LOGGER = Logger.getLogger(XNATProperties.class);
    public static final String[] MAIN_USER = seleniumAliasedProperty("xnat.main.user");
    public static final String[] MAIN_PASS = seleniumAliasedProperty("xnat.main.password");
    public static final String[] MAIN_ADMIN_USER = seleniumAliasedProperty("xnat.mainAdmin.user");
    public static final String[] MAIN_ADMIN_PASS = seleniumAliasedProperty("xnat.mainAdmin.password");
    public static final String ADMIN_USER = "xnat.admin.user";
    public static final String ADMIN_PASS = "xnat.admin.password";
    public static final String XNAT_VERSION = "xnat.version";
    public static final String BASEURL = "xnat.baseurl";
    public static final String EMAIL = "xnat.users.email";
    public static final String EMAIL_PASS = "xnat.users.email.password";
    public static final String DEFAULT_TIMEOUT = "xnat.defaultTimeout";
    public static final String BROWSER = "xnat.browser";
    public static final String INIT_SETTING = "xnat.init";
    public static final String TEMP_DIR = "xnat.temp";
    public static final String DICOM_HOST = "xnat.dicom.host";
    public static final String DICOM_PORT = "xnat.dicom.port";
    public static final String DICOM_AETITLE = "xnat.dicom.aetitle";
    public static final String JIRA_SETTING = "xnat.jira";
    public static final String ADMIN_SETTING = "xnat.requireAdmin";
    public static final String DYNAMIC_ORDERING = "xnat.pipeline.useDynamicOrdering";
    public static final String GLPK_AVAILABLE = "xnat.pipeline.glpk";
    public static final String QUEUE_SLOTS = "xnat.pipeline.slots";
    public static final String NOTIFICATION_EMAILS = "xnat.notifiedEmails";
    public static final String NOTIFICATION_SETTING = "xnat.notifyOnSuccess";
    public static final String NOTIFICATION_TITLE = "xnat.notificationTitle";
    public static final String CHECK_DEPENDENCIES = "xnat.dependencies";
    public static final String TIMELOG_SETTING = "xnat.timelogs";
    public static final String GITLOGS_SETTING = "xnat.gitLogs";
    public static final String BASIC_MODE = "xnat.basic";
    public static final String DATABASE_URL = "xnat.db.url";
    public static final String DATABASE_USER = "xnat.db.user";
    public static final String DATABASE_PASS = "xnat.db.password";
    public static final String SSH_USER = "xnat.ssh.user";
    public static final String SSH_PRIVATE_KEY_NAME = "xnat.ssh.key";
    public static final String PRODUCE_PDF = "xnat.producePdf";
    public static final String DOM_SETTING = "xnat.captureDom";
    public static final String TOMCAT_VERSION = "tomcat.version";
    public static final String XNAT_REQUIRED = "xnat.required"; // used to specify that additional XNATs will be needed

    public XNATProperties() {
        super("xnat.config", "local.properties");
        properties = getPropertiesFromFile();
    }

    private String nthXnatProperty(String basePropertyName, int n) {
        return basePropertyName.replace("xnat.", String.format("xnat%d.", n));
    }

    private static String[] seleniumAliasedProperty(String normalProperty) {
        return new String[]{normalProperty, normalProperty.replace("main", "selenium")};
    }

    public String getBaseURL() {
        return getPropertyFromAnywhere(BASEURL);
    }

    public String getAdminUser() {
        return getPropertyFromAnywhere(ADMIN_USER);
    }

    public String getAdminPassword() {
        return getSensitiveProperty(ADMIN_PASS);
    }

    public String getMainUser() {
        return getPropertyFromAnywhere(MAIN_USER);
    }

    public String getMainPassword() {
        return getSensitiveProperty(MAIN_PASS);
    }

    public String getMainEmail() {
        return getPropertyFromAnywhere(EMAIL);
    }

    public String getMainEmailPassword() {
        return getSensitiveProperty(EMAIL_PASS);
    }

    public String getMainAdminUser() {
        return getPropertyFromAnywhere(MAIN_ADMIN_USER);
    }

    public String getMainAdminPassword() {
        return getSensitiveProperty(MAIN_ADMIN_PASS);
    }

    public int getDefaultTimeout() {
        return getIntProperty(DEFAULT_TIMEOUT, 30);
    }

    public String getBrowser() {
        return getStringProperty(false, BROWSER, "Firefox");
    }

    public boolean getInitSetting() {
        return getBooleanProperty(INIT_SETTING, true);
    }

    public String getTempFolder() {
        if (getPropertyFromAnywhere(TEMP_DIR) == null) {
            String temp = System.getProperty("java.io.tmpdir");
            if (temp.endsWith(File.separator)) {
                // In Linux, this reports the temp folder without a trailing slash, but it has one in Windows.
                temp = temp.substring(0, temp.length() - 1);
            }
            return temp;
        }
        return getPropertyFromAnywhere(TEMP_DIR);
    }

    public String getDicomHost() {
        return getPropertyFromAnywhere(DICOM_HOST);
    }

    public int getDicomPort() {
        return getIntProperty(DICOM_PORT, -1);
    }

    public String getDicomAetitle() {
        return getPropertyFromAnywhere(DICOM_AETITLE);
    }

    public boolean getJiraSetting() {
        return getBooleanProperty(JIRA_SETTING, false);
    }

    public boolean getAdminSetting() {
        return getBooleanProperty(ADMIN_SETTING, true);
    }

    public boolean getDynamicOrderingSetting() {
        return getBooleanProperty(DYNAMIC_ORDERING, false);
    }

    public boolean getGLPKSetting() {
        return getBooleanProperty(GLPK_AVAILABLE, false);
    }

    public int getQueueSlots() {
        return getIntProperty(QUEUE_SLOTS, 0);
    }

    public String[] getNotificationEmails() {
        String emailString = getPropertyFromAnywhere(NOTIFICATION_EMAILS);
        if (emailString == null || emailString.equals("")) return null;
        return emailString.split(",");
    }

    public boolean getNotificationSetting() {
        return getBooleanProperty(NOTIFICATION_SETTING, false);
    }

    public String getNotificationTitle() {
        return getPropertyFromAnywhere(NOTIFICATION_TITLE);
    }

    public boolean getDependencyCheck() {
        return getBooleanProperty(CHECK_DEPENDENCIES, true);
    }

    public boolean getTimelogSetting() {
        return getBooleanProperty(TIMELOG_SETTING, false);
    }

    public boolean getGitlogSetting() {
        return getBooleanProperty(GITLOGS_SETTING, false);
    }

    public boolean getBasicSetting() {
        return getBooleanProperty(BASIC_MODE, false);
    }

    public String getDatabaseUrl() {
        return getSensitiveProperty(DATABASE_URL);
    }

    public String getDatabaseUser() {
        return getSensitiveProperty(DATABASE_USER);
    }

    public String getDatabasePass() {
        return getSensitiveProperty(DATABASE_PASS);
    }

    public String getSshUser() {
        return getPropertyFromAnywhere(SSH_USER);
    }

    public String getSshPrivateKeyName() {
        return getStringProperty(false, SSH_PRIVATE_KEY_NAME, "id_rsa");
    }

    public boolean getPdfSetting() {
        return getBooleanProperty(PRODUCE_PDF, false);
    }

    public boolean getDomSetting() {
        return getBooleanProperty(DOM_SETTING, false);
    }

    public String getXNATVersion() {
        final String version = getPropertyFromAnywhere(XNAT_VERSION);
        if (version == null) {
            LOGGER.fatal(String.format("Required setting %s was not set.", XNAT_VERSION));
            throw new RuntimeException(String.format("Required setting %s was not set.", XNAT_VERSION));
        }

        return version;
    }

    public String getTomcatVersion() {
        return getStringProperty(false, TOMCAT_VERSION, "tomcat7");
    }

    public boolean nthXnatRequired(int n) {
        return getBooleanProperty(nthXnatProperty(XNAT_REQUIRED, n), false);
    }

    public List<XnatConfig> getOtherXnatConfigs() {
        final List<XnatConfig> bonusConfigs = new ArrayList<>();
        int configIndex = 2;
        while (true) {
            if (nthXnatRequired(configIndex)) {
                bonusConfigs.add(new XnatConfig().
                        mainUsername(getPropertyFromAnywhere(nthXnatProperty(MAIN_USER[0], configIndex))).
                        mainPassword(getSensitiveProperty(nthXnatProperty(MAIN_PASS[0], configIndex))).
                        mainAdminUsername(getPropertyFromAnywhere(nthXnatProperty(MAIN_ADMIN_USER[0], configIndex))).
                        mainAdminPassword(getSensitiveProperty(nthXnatProperty(MAIN_ADMIN_PASS[0], configIndex))).
                        adminUsername(getPropertyFromAnywhere(nthXnatProperty(ADMIN_USER, configIndex))).
                        adminPassword(getSensitiveProperty(nthXnatProperty(ADMIN_PASS, configIndex))).
                        xnatVersion(getPropertyFromAnywhere(nthXnatProperty(XNAT_VERSION, configIndex))).
                        xnatUrl(CommonUtils.formatUrl(getPropertyFromAnywhere(nthXnatProperty(BASEURL, configIndex)))).
                        init(getBooleanProperty(nthXnatProperty(INIT_SETTING, configIndex), true)).
                        build()
                );
            } else {
                break;
            }
            configIndex++;
        }
        return bonusConfigs;
    }

}