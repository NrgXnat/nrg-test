package org.nrg.testing.jira;

import org.nrg.testing.util.BaseProperties;

public class JIRAProperties extends BaseProperties {

    public static final String JIRA_CONFIG = "jira.config";
    public static final String JIRA_USER = "jira.user";
    public static final String JIRA_PASS = "jira.password";
    public static final String JIRA_URL = "jira.url";
    public static final String JIRA_PROJECT = "jira.project";
    public static final String JIRA_PROJECT_VERSION = "jira.project.version";
    public static final String JIRA_CYCLE_NAME = "jira.cycleName";
    public static final String JIRA_STEPS = "jira.steps";

    public JIRAProperties() {
        super(JIRA_CONFIG, "jira.properties");
    }

    public String getJiraURL() {
        return getPropertyFromAnywhere(JIRA_URL);
    }

    public String getJiraUser() {
        return getPropertyFromAnywhere(JIRA_USER);
    }

    public String getJiraPassword() {
        return getSensitiveProperty(JIRA_PASS);
    }

    public String getJiraProject() {
        return getPropertyFromAnywhere(JIRA_PROJECT);
    }

    public String getJiraProjectVersion() {
        return getPropertyFromAnywhere(JIRA_PROJECT_VERSION);
    }

    public String getJiraCycleName() {
        return getStringProperty(false, JIRA_CYCLE_NAME, "XNAT test suite cycle");
    }

    public boolean getStepCondition() {
        return getBooleanProperty(JIRA_STEPS, false);
    }

}