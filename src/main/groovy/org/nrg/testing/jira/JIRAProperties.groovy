package org.nrg.testing.jira

import org.nrg.testing.util.BaseProperties

class JIRAProperties extends BaseProperties {

    public static final String JIRA_CONFIG = 'jira.config'
    public static final String JIRA_USER = 'jira.user'
    public static final String JIRA_PASS = 'jira.password'
    public static final String JIRA_URL = 'jira.url'
    public static final String JIRA_PROJECT = 'jira.project'
    public static final String JIRA_PROJECT_VERSION = 'jira.project.version'
    public static final String JIRA_CYCLE_NAME = 'jira.cycleName'
    public static final String JIRA_STEPS = 'jira.steps'

    JIRAProperties() {
        super(JIRA_CONFIG, 'jira.properties')
    }

    String getJiraURL() {
        getPropertyFromAnywhere(JIRA_URL)
    }

    String getJiraUser() {
        getPropertyFromAnywhere(JIRA_USER)
    }

    String getJiraPassword() {
        getSensitiveProperty(JIRA_PASS)
    }

    String getJiraProject() {
        getPropertyFromAnywhere(JIRA_PROJECT)
    }

    String getJiraProjectVersion() {
        getPropertyFromAnywhere(JIRA_PROJECT_VERSION)
    }

    String getJiraCycleName() {
        getStringProperty(false, JIRA_CYCLE_NAME, 'XNAT test suite cycle')
    }

    boolean getStepCondition() {
        getBooleanProperty(JIRA_STEPS, false)
    }

}