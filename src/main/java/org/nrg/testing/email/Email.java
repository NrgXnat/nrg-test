package org.nrg.testing.email;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import java.util.ArrayList;
import java.util.List;

import static org.testng.AssertJUnit.*;

public class Email {

    private List<EmailAttachment> attachments = new ArrayList<>();
    private Document document;
    private String subject;
    private String emailContent;

    public Email(Message message) {
        try {
            subject = message.getSubject();
            document = Jsoup.parse(EmailReader.getText(message));
            emailContent = document.body().html();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read in email.", e);
        }
        try {
            if (message.getContent() instanceof Multipart) {
                final Multipart multipart = (Multipart)message.getContent();
                for (int i = 0; i < multipart.getCount(); i++) {
                    BodyPart part = multipart.getBodyPart(i);
                    if (StringUtils.isNotBlank(part.getDisposition()) && StringUtils.isNotBlank(part.getFileName()) && Part.ATTACHMENT.equals(part.getDisposition().toLowerCase())) {
                        attachments.add(new EmailAttachment(part));
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read in attachments.", e);
        }
    }

    public List<EmailAttachment> getAttachments() {
        return attachments;
    }

    public Document getDocument() {
        return document;
    }

    public String getSubject() {
        return subject;
    }

    public String getEmailContent() {
        return emailContent;
    }

    public String extractSingleLink() {
        final Elements links = document.select("a[href]");
        if (links.size() != 1) {
            fail(String.format("Search through email attempted to find exactly 1 link (a href). Instead, %d links were found.", links.size()));
        }
        return removeAmpersandEncoding(links.get(0).attr("abs:href"));
    }

    public String extractLinkByText(String text) {
        final Elements links = document.select("a[href]");
        for (Element element : links) {
            if (text.equals(element.text())) return removeAmpersandEncoding(element.attr("abs:href"));
        }
        fail("No link found by text: " + text); throw new AssertionError("Unreachable");
    }

    public String removeAmpersandEncoding(String string) {
        return string.replace("amp;", "");
    }

    public void assertEmailContains(String contained) {
        assertTrue(emailContent.contains(contained));
    }

    public void assertEmailEquals(String emailText) {
        assertEquals(emailText, emailContent);
    }

    public void assertSubjectEquals(String subject) {
        assertEquals(subject, this.subject);
    }

}
