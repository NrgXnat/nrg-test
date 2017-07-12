/*
 * org.nrg.xnat.selenium.EmailReader
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 4/30/14 10:50 AM
 *
 * Adapted from http://www.compiletimeerror.com/2013/06/reading-email-using-javamail-api-example.html#.U17GNvldVUV
 */

package org.nrg.testing.email;

import org.apache.commons.lang3.time.StopWatch;
import org.nrg.testing.xnat.conf.Settings;

import javax.mail.*;
import javax.mail.search.SearchTerm;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class EmailReader {

    private static final int DEFAULT_EMAIL_TIMEOUT = 10 * Settings.DEFAULT_TIMEOUT;
    private static final int DEFAULT_EMAILS_TO_CHECK = 5;
    private static final int DEFAULT_NUM_EMAILS_TO_FIND = 1;
    private static final Properties props = getEmailProperties();
    private final StopWatch stopWatch = new StopWatch();
    private Store store;
    private Folder inbox;
    private SearchTerm searchTerm;
    private int timeout = DEFAULT_EMAIL_TIMEOUT;
    private int emailsToCheck = DEFAULT_EMAILS_TO_CHECK;
    private int emailsToFind = DEFAULT_NUM_EMAILS_TO_FIND;

    private static Properties getEmailProperties() {
        Properties prop = new Properties();
        prop.setProperty("mail.store.protocol", "imaps");
        return prop;
    }

    public EmailReader(SearchTerm searchTerm) {
        this.searchTerm = searchTerm;
    }

    public EmailReader(String searchString) {
        this(SearchTerms.bodyContains(searchString));
    }

    public EmailReader setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public EmailReader checkEmails(int numEmails) {
        this.emailsToCheck = numEmails;
        return this;
    }

    public EmailReader findEmails(int numEmails) {
        this.emailsToFind = numEmails;
        return this;
    }

    private void connectToGmail() {
        try {
            Session session = Session.getInstance(props, null);
            store = session.getStore();
            store.connect("imap.gmail.com", Settings.EMAIL, Settings.EMAIL_PASS);
            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);
        } catch (Exception mex) {
            throw new RuntimeException("Failed in connecting to gmail", mex);
        }
    }

    private void closeGmailConnection() {
        try {
            store.close();
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to close gmail connection" + e);
        }
    }

    private Message[] getNewestEmails(int numberOfEmails) {
        final Message[] emails;
        try {
            final int inboxSize = inbox.getMessageCount();
            final int numMessagesToSearch = Math.min(numberOfEmails, inboxSize); // can't search more messages than inbox contains
            emails = inbox.getMessages(inboxSize - numMessagesToSearch, inboxSize);
        } catch (Exception mex) {
            throw new RuntimeException("Failed to get recent emails", mex);
        }
        return emails;
    }

    /**
     * Return the primary text content of the message.
     */
    public static String getText(Part p) throws MessagingException, IOException {
        // From http://www.oracle.com/technetwork/java/javamail/faq/index.html#mainbody
        if (p.isMimeType("text/*")) {
            return (String)p.getContent();
        }

        if (p.isMimeType("multipart/alternative")) {
            // prefer html text over plain text
            Multipart mp = (Multipart)p.getContent();
            String text = null;
            for (int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) {
                    if (text == null)
                        text = getText(bp);
                } else if (bp.isMimeType("text/html")) {
                    String s = getText(bp);
                    if (s != null)
                        return s;
                } else {
                    return getText(bp);
                }
            }
            return text;
        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart)p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String s = getText(mp.getBodyPart(i));
                if (s != null)
                    return s;
            }
        }
        return null;
    }

    public List<Email> getEmails() {
        Message[] searchedMessages, messages;
        final List<Email> returnedMessages = new ArrayList<>();
        startStopWatch();

        while (true) {
            if (stopWatch.getTime() > timeout * 1000) {
                // stopWatch.getTime() gives milliseconds
                closeGmailConnection();
                throw new RuntimeException("Emails not received in time");
            }
            connectToGmail();
            messages = getNewestEmails(emailsToCheck);

            try {
                searchedMessages = inbox.search(searchTerm, messages);
            } catch (MessagingException e) {
                throw new RuntimeException("Failed to search email messages correctly " + e);
            }

            if (searchedMessages.length == emailsToFind) {
                for (Message searchedMessage : searchedMessages) {
                    returnedMessages.add(new Email(searchedMessage));
                }
                closeGmailConnection();
                return returnedMessages;
            }
            closeGmailConnection();
        }
    }

    public Email getEmail() {
        emailsToFind = 1;
        return getEmails().get(0);
    }

    private void startStopWatch() {
        stopWatch.reset();
        stopWatch.start();
    }

}