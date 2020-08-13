package org.nrg.testing.listeners.adapters.jira.failure

import org.nrg.jira.JiraController

class FailureWithExistingBug implements FailureCause {

    private String reason
    private String bug
    private JiraController jiraController

    FailureWithExistingBug(String reason, String bug, JiraController jiraController) {
        this.reason = reason
        this.bug = bug
        this.jiraController = jiraController
    }

    @Override
    String getHTMLReason() {
        "${reason}. See: ${getBugLink()}"
    }

    private String getBugLink() {
        (jiraController == null) ? bug : "<a href=\"${jiraController.formatJiraUrl('browse', bug)}\">${bug}</a>"
    }

}
