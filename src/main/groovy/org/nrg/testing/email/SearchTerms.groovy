package org.nrg.testing.email

import org.nrg.testing.xnat.versions.XnatTestingVersionManager
import org.nrg.xnat.versions.Xnat_1_7_4
import org.nrg.xnat.pogo.Project
import org.nrg.xnat.pogo.users.User

import javax.mail.Message
import javax.mail.search.SearchTerm

class SearchTerms {

    /**
     * Search criteria for emails
     */

    static SearchTerm and(final SearchTerm... searchTerms) {
        new SearchTerm() {
            @Override
            boolean match(Message message) {
                !searchTerms.any { term ->
                    !term.match(message)
                }
            }
        }
    }

    static SearchTerm or(final SearchTerm... searchTerms) {
        new SearchTerm() {
            @Override
            boolean match(Message message) {
                searchTerms.any { term ->
                    term.match(message)
                }
            }
        }
    }

    static SearchTerm not(final SearchTerm searchTerm) {
        new SearchTerm() {
            @Override
            boolean match(Message message) {
                !searchTerm.match(message)
            }
        }
    }

    static SearchTerm bodyContains(final String search) {
        new SearchTerm() {
            @Override
            boolean match(Message message) {
                final String messageBody = EmailReader.getText(message)
                messageBody != null && messageBody.contains(search)
            }
        }
    }

    static SearchTerm bodyContainsAll(final List<String> stringList) {
        and(containsArray(stringList))
    }

    static SearchTerm bodyContainsSome(final List<String> stringList) {
        or(containsArray(stringList))
    }

    static SearchTerm bodyContainsThisButNotThat(final String goodString, final String badString) {
        and(bodyContains(goodString), not(bodyContains(badString)))
    }

    /**
     * SearchTerm that is satisfied iff the email body contains every String in requiredStrings and none of the Strings in prohibitedStrings
     * @param requiredStrings List of Strings to check the message for
     * @param prohibitedStrings List of Strings to exclude messages with
     * @return SearchTerm object
     */
    static SearchTerm bodyContains(final List<String> requiredStrings, final List<String> prohibitedStrings) {
        and(bodyContainsAll(requiredStrings), not(bodyContainsSome(prohibitedStrings)))
    }

    static SearchTerm receivedAfter(final Date date) {
        new SearchTerm() {
            @Override
            boolean match(Message message) {
                date.before(message.receivedDate)
            }
        }
    }

    static SearchTerm verificationEmail(User user) {
        bodyContainsAll([
                "${user.firstName} ${user.lastName}",
                (XnatTestingVersionManager.testedVersionFollows(Xnat_1_7_4)) ? 'If you would like to register, please confirm your email address' : 'Please click this link to verify your email address'
        ])
    }

    static SearchTerm projectAccessEmail(Project project, boolean approved) {
        final String search = {
            if (XnatTestingVersionManager.testedVersionFollows(Xnat_1_7_4)) {
                approved ? "You have been granted access to the ${project.title} project" : "request to access the ${project.title} project has been denied"
            } else {
                "${project.title} access ${approved ? 'granted' : 'denied'}"
            }
        }.call()

        bodyContains(search)
    }

    private static SearchTerm[] containsArray(final List<String> stringList) {
        stringList.collect { term ->
            bodyContains(term)
        } as SearchTerm[]
    }

}
