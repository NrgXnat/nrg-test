package org.nrg.testing.listeners.adapters.jira.failure

class FailureExplanation implements FailureCause {

    private String reason

    FailureExplanation(String reason) {
        this.reason = reason
    }

    @Override
    String getHTMLReason() {
        reason
    }

}
