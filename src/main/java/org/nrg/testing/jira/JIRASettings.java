/*
 * org.nrg.selenium.jira.JIRASettings
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.testing.jira;

import org.nrg.testing.xnat.conf.XNATProperties;

public class JIRASettings {

    private static final JIRAProperties properties = new JIRAProperties();
    public static final String JIRA_URL = properties.getJiraURL();
    public static final String PROJECT = properties.getJiraProject();
    public static final String VERSION = properties.getJiraProjectVersion();
    public static final String JIRA_USER = properties.getJiraUser();
    public static final String JIRA_PASS = properties.getJiraPassword();
    public static final String JIRA_CYCLE_NAME = properties.getJiraCycleName();
    public static final boolean STEP_SCREENSHOTS = properties.getStepCondition();

}
