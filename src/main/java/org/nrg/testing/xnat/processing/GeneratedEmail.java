package org.nrg.testing.xnat.processing;

import org.nrg.testing.email.Email;
import org.nrg.testing.email.EmailReader;
import org.nrg.testing.email.SearchTerms;

import javax.mail.search.SearchTerm;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GeneratedEmail implements ProcessingCheckable {

    private int numEmailsToCheck = 20;
    private String name;
    private static Date suiteStartTime;
    private final List<SearchTerm> matchCriteria = new ArrayList<>();
    private String subjectCheck;

    public static void setSuiteStartTime() {
        suiteStartTime = new Date();
    }

    public GeneratedEmail(String friendlyName) {
        name = friendlyName;
    }

    public void addMessageContainCriterion(String contains) {
        matchCriteria.add(SearchTerms.bodyContains(contains));
    }

    public void addSubjectCheck(String subject) {
        subjectCheck = subject;
    }

    public String checkEmail() {
        final SearchTerm customCriteria = SearchTerms.and(matchCriteria.toArray(new SearchTerm[]{}));
        final Email email;

        try {
            email = new EmailReader(SearchTerms.and(customCriteria, SearchTerms.receivedAfter(suiteStartTime))).checkEmails(numEmailsToCheck).getEmail();
        } catch (Exception e) {
            return "Could not locate email: " + name;
        }

        if (subjectCheck != null) {
            try {
                email.assertSubjectEquals(subjectCheck);
            } catch (Exception | Error e) {
                return String.format("Email %s had incorrect subject: %s", name, email.getSubject());
            }
        }
        return null;
    }
}
