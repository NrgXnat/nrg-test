package org.nrg.testing.xnat.processing

import org.nrg.testing.email.Email
import org.nrg.testing.email.EmailReader
import org.nrg.testing.email.SearchTerms

import javax.mail.search.SearchTerm

class GeneratedEmail implements ProcessingCheckable {

    private static Date suiteStartTime
    private static final int NUM_EMAILS_TO_CHECK = 20
    private String name
    private final List<SearchTerm> matchCriteria = [SearchTerms.receivedAfter(suiteStartTime)]
    private String subjectCheck

    static void setSuiteStartTime() {
        suiteStartTime = new Date()
    }

    GeneratedEmail(String friendlyName) {
        name = friendlyName
    }

    void addMessageContainCriterion(String contains) {
        matchCriteria << SearchTerms.bodyContains(contains)
    }

    void addSubjectCheck(String subject) {
        subjectCheck = subject
    }

    String checkEmail() {
        final SearchTerm customCriteria = SearchTerms.and(matchCriteria as SearchTerm[])
        final Email email

        try {
            email = new EmailReader(SearchTerms.and(matchCriteria as SearchTerm[])).checkEmails(NUM_EMAILS_TO_CHECK).email
        } catch (Exception ignored) {
            return "Could not locate email: ${name}"
        }

        if (subjectCheck != null) {
            final String actualSubject = email.subject
            if (actualSubject != subjectCheck) {
                return "Email ${name} had incorrect subject: ${actualSubject}"
            }
        }
        null
    }
}
