package org.nrg.testing.email

import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

import javax.mail.BodyPart
import javax.mail.Message
import javax.mail.Multipart
import javax.mail.Part

import static org.testng.AssertJUnit.*

class Email {

    final List<EmailAttachment> attachments = []
    Document document
    String subject
    String emailContent

    Email(Message message) {
        subject = message.subject
        document = Jsoup.parse(EmailReader.getText(message))
        emailContent = document.body().html()
        if (message.content instanceof Multipart) {
            final Multipart multipart = message.content as Multipart
            (0 ..< multipart.count).each { i ->
                final BodyPart part = multipart.getBodyPart(i)
                if (StringUtils.isNotBlank(part.disposition) && StringUtils.isNotBlank(part.fileName) && Part.ATTACHMENT == part.disposition.toLowerCase()) {
                    attachments << new EmailAttachment(part)
                }
            }
        }
    }

    String extractSingleLink() {
        final Elements links = readLinks()
        if (links.size() != 1) {
            fail("Search through email attempted to find exactly 1 link (<a href ...>...</a>). Instead, ${links.size()} links were found.")
        }
        removeAmpersandEncoding(links[0].attr('abs:href'))
    }

    String extractFirstLink() {
        removeAmpersandEncoding(readLinks()[0].attr('abs:href'))
    }

    @SuppressWarnings('GroovyMissingReturnStatement')
    String extractLinkByText(String text) {
        final Element link = readLinks().find { possibleLink ->
            text == possibleLink.text()
        }
        if (link != null) {
            removeAmpersandEncoding(link.attr('abs:href'))
        } else {
            fail("No link found by text: ${text}")
        }
    }

    private Elements readLinks() {
        document.select('a[href]')
    }

    String removeAmpersandEncoding(String string) {
        string.replace('&amp;', '&')
    }

    void assertEmailContains(String contained) {
        assertTrue(emailContent.contains(contained))
    }

    void assertEmailEquals(String emailText) {
        assertEquals(emailText, emailContent)
    }

    void assertSubjectEquals(String subject) {
        assertEquals(subject, this.subject)
    }

}
