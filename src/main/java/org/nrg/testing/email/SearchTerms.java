package org.nrg.testing.email;

import org.nrg.testing.xnat.versions.XnatVersionLineage;
import org.nrg.testing.xnat.versions.Xnat_1_7_4;
import org.nrg.xnat.pogo.Project;
import org.nrg.xnat.pogo.users.User;

import javax.mail.Message;
import javax.mail.search.SearchTerm;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class SearchTerms {

    /**
     * Search criteria for emails
     */

    public static SearchTerm and(final SearchTerm... searchTerms) {
        return new SearchTerm() {
            @Override
            public boolean match(Message message) {
                for (SearchTerm term : searchTerms) {
                    if (!term.match(message)) return false;
                }
                return true;
            }
        };
    }

    public static SearchTerm or(final SearchTerm... searchTerms) {
        return new SearchTerm() {
            @Override
            public boolean match(Message message) {
                for (SearchTerm term : searchTerms) {
                    if (term.match(message)) return true;
                }
                return false;
            }
        };
    }

    public static SearchTerm not(final SearchTerm searchTerm) {
        return new SearchTerm() {
            @Override
            public boolean match(Message message) {
                return !searchTerm.match(message);
            }
        };
    }

    public static SearchTerm bodyContains(final String search) {
        return new SearchTerm() {
            @Override
            public boolean match(Message message) {
                try {
                    final String messageBody = EmailReader.getText(message);
                    return messageBody != null && messageBody.contains(search);
                } catch (Exception ex) {
                    throw new RuntimeException("Failed to get messages", ex);
                }
            }
        };
    }

    public static SearchTerm bodyContainsAll(final List<String> stringList) {
        return and(containsArray(stringList));
    }

    public static SearchTerm bodyContainsSome(final List<String> stringList) {
        return or(containsArray(stringList));
    }

    public static SearchTerm bodyContainsThisButNotThat(final String goodString, final String badString) {
        return and(bodyContains(goodString), not(bodyContains(badString)));
    }

    /**
     * SearchTerm that is satisfied iff the email body contains every String in requiredStrings and none of the Strings in prohibitedStrings
     * @param requiredStrings List of Strings to check the message for
     * @param prohibitedStrings List of Strings to exclude messages with
     * @return SearchTerm object
     */
    public static SearchTerm bodyContains(final List<String> requiredStrings, final List<String> prohibitedStrings) {
        return and(bodyContainsAll(requiredStrings), not(bodyContainsSome(prohibitedStrings)));
    }

    public static SearchTerm receivedAfter(final Date date) {
        return new SearchTerm() {
            @Override
            public boolean match(Message message) {
                try {
                    return date.before(message.getReceivedDate());
                } catch (Exception e) {
                    throw new RuntimeException("Failed to filter messages by received date.", e);
                }
            }
        };
    }

    public static SearchTerm verificationEmail(User user) {
        return bodyContainsAll(Arrays.asList(
                String.format("Dear %s %s", user.getFirstName(), user.getLastName()),
                (XnatVersionLineage.testedVersionFollows(Xnat_1_7_4.class)) ? "If you would like to register, please confirm your email address" : "Please click this link to verify your email address")
        );
    }

    public static SearchTerm projectAccessEmail(Project project, boolean approved) {
        return bodyContains(
                XnatVersionLineage.testedVersionFollows(Xnat_1_7_4.class) ?
                    (approved ? String.format("You have been granted access to the %s project", project.getTitle()) : String.format("request to access the %s project has been denied", project.getTitle())) :
                    String.format("%s access %s",project.getTitle(), (approved) ? "granted" : "denied")
        );
    }

    private static SearchTerm[] containsArray(final List<String> stringList) {
        final List<SearchTerm> terms = new ArrayList<>();
        for (String term : stringList) {
            terms.add(bodyContains(term));
        }
        return terms.toArray(new SearchTerm[]{});
    }

}
