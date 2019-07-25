package org.nrg.testing.email

import org.apache.commons.lang3.time.StopWatch
import org.nrg.testing.TimeUtils
import org.nrg.testing.xnat.conf.Settings
import org.nrg.testing.xnat.versions.XnatVersionList
import org.nrg.testing.xnat.versions.Xnat_1_7_4
import org.nrg.xnat.pogo.users.User

import javax.mail.*
import javax.mail.search.SearchTerm

class EmailReader {

    private static final Properties props = getEmailProperties()
    private final StopWatch stopWatch = new StopWatch()
    private Store store
    private Folder inbox
    private SearchTerm searchTerm
    private int timeout = 10 * Settings.DEFAULT_TIMEOUT
    private int emailsToCheck = 5
    private int emailsToFind = 1

    private static Properties getEmailProperties() {
        Properties prop = new Properties()
        prop.setProperty('mail.store.protocol', 'imaps')
        return prop
    }

    EmailReader() {}

    EmailReader(SearchTerm searchTerm) {
        this.searchTerm = searchTerm
    }

    EmailReader(String searchString) {
        this(SearchTerms.bodyContains(searchString))
    }

    EmailReader setTimeout(int timeout) {
        this.timeout = timeout
        this
    }

    EmailReader checkEmails(int numEmails) {
        this.emailsToCheck = numEmails
        this
    }

    EmailReader findEmails(int numEmails) {
        this.emailsToFind = numEmails
        this
    }

    String readVerificationEmailLink(User user) {
        searchTerm = SearchTerms.verificationEmail(user)
        XnatVersionList.testedVersionFollows(Xnat_1_7_4) ? getEmail().extractFirstLink() : getEmail().extractSingleLink()
    }

    List<Email> getEmails() {
        startStopWatch()

        while (true) {
            if (TimeUtils.maxTimeReached(stopWatch, timeout)) {
                store.close()
                throw new RuntimeException('Emails not received in time')
            }
            connectToGmail()
            final Message[] searchedMessages = inbox.search(searchTerm, getNewestEmails(emailsToCheck))
            if (searchedMessages.length == emailsToFind) {
                final List<Email> returnedMessages = searchedMessages.collect { message ->
                    new Email(message)
                }
                store.close()
                return returnedMessages
            }
            store.close()
        }
    }

    Email getEmail() {
        emailsToFind = 1
        getEmails()[0]
    }

    /**
     * Return the primary text content of the message.
     */
    static String getText(Part p) throws MessagingException, IOException {
        // From http://www.oracle.com/technetwork/java/javamail/faq/index.html#mainbody
        if (p.isMimeType('text/*')) {
            return p.content as String
        }

        if (p.isMimeType('multipart/alternative')) {
            // prefer html text over plain text
            final Multipart mp = p.content as Multipart
            String text = null
            (0 ..< mp.count).each { i ->
                final Part bp = mp.getBodyPart(i)
                if (bp.isMimeType('text/plain') && text == null) {
                    text = getText(bp)
                } else if (bp.isMimeType('text/html')) {
                    final String s = getText(bp)
                    if (s != null) {
                        return s
                    }
                } else {
                    return getText(bp)
                }
            }
            return text
        } else if (p.isMimeType('multipart/*')) {
            final Multipart mp = p.content as Multipart
            (0 ..< mp.count).each { i ->
                final String s = getText(mp.getBodyPart(i))
                if (s != null) {
                    return s
                }
            }
        }
        null
    }

    private void connectToGmail() {
        final String email = Settings.EMAIL
        if (!email.endsWith('@gmail.com')) {
            throw new IllegalArgumentException('If attempting to read email to verify test results, email address must be an @gmail.com address')
        }
        final Session session = Session.getInstance(props, null)
        store = session.store
        store.connect('imap.gmail.com', email, Settings.EMAIL_PASS)
        inbox = store.getFolder('INBOX')
        inbox.open(Folder.READ_ONLY)
    }

    private Message[] getNewestEmails(int numberOfEmails) {
        final int inboxSize = inbox.messageCount
        final int numMessagesToSearch = Math.min(numberOfEmails, inboxSize) // can't search more messages than inbox contains
        inbox.getMessages(inboxSize - numMessagesToSearch, inboxSize)
    }

    private void startStopWatch() {
        stopWatch.reset()
        stopWatch.start()
    }

}