package org.nrg.testing.email

import com.google.api.services.gmail.model.Message
import com.google.api.services.gmail.model.MessagePart
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.nrg.testing.xnat.versions.XnatTestingVersionManager
import org.nrg.xnat.versions.Xnat_1_7_4

import static org.testng.AssertJUnit.*

class ParsedEmail {

    List<EmailAttachment> attachments = []
    Document document
    String subject

    String getEmailContent() {
        document.body().html()
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

    String readVerificationEmailLink() {
        XnatTestingVersionManager.testedVersionFollows(Xnat_1_7_4) ? extractFirstLink() : extractSingleLink()
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

    private Elements readLinks() {
        document.select('a[href]')
    }

    static String resolveParts(MessagePart messagePart) {
        // Adapted generally from http://www.oracle.com/technetwork/java/javamail/faq/index.html#mainbody
        final String mimeType = messagePart.getMimeType()

        if (mimeType.startsWith('text/')) {
            return new String(messagePart.getBody().getData().decodeBase64Url())
        }

        if (mimeType == 'multipart/alternative') {
            // prefer html text over plain text
            String text = null
            return (0 ..< messagePart.getParts().size()).findResult { i ->
                final MessagePart bp = messagePart.getParts()[i]
                if (bp.getMimeType() == 'text/plain' && text == null) {
                    text = resolveParts(bp)
                } else if (bp.getMimeType() == 'text/html') {
                    final String s = resolveParts(bp)
                    if (s != null) {
                        return s
                    }
                } else {
                    return resolveParts(bp)
                }
                null
            } ?: text
        }

        if (mimeType.startsWith('multipart/')) {
            return (0 ..< messagePart.getParts().size()).findResult { i ->
                resolveParts(messagePart.getParts()[i])
            }
        }

        null
    }

    static ParsedEmail fromId(String id) {
        final Message message = EmailClient.retrieveFullMessage(id)
        final MessagePart payload = message.getPayload()
        final String mimeType = payload.getMimeType()
        final String subject = payload.getHeaders().find { header ->
            header.getName() == 'Subject'
        }.getValue()
        final Document document = Jsoup.parse(resolveParts(payload))
        final List<EmailAttachment> attachments = []

        if (mimeType.startsWith('multipart/')) {
            (0 ..< payload.getParts().size()).each { i ->
                final MessagePart messagePart = payload.getParts()[i]
                final String contentDisposition = messagePart.getHeaders().find { header ->
                    header.name == 'Content-Disposition'
                }?.getValue() ?: ''
                if (contentDisposition.startsWith('attachment') && messagePart.getFilename() != null) {
                    final String attachmentData = messagePart.getBody().getData()
                    final String attachmentId = messagePart.getBody().getAttachmentId()
                    final EmailAttachment attachment = new EmailAttachment(
                            fileName: messagePart.getFilename(),
                            contentType: messagePart.getMimeType()
                    )
                    if (attachmentData != null) {
                        attachment.setBase64UrlContents(attachmentData)
                    } else if (attachment.contentType.contains('text/plain')) {
                        attachment.setBase64UrlContents(EmailClient.retrieveAttachment(message.getId(), attachmentId).getData())
                    }
                    attachments << attachment
                }
            }
        }

        new ParsedEmail(
                subject: subject,
                document: document,
                attachments: attachments
        )
    }

    static String removeAmpersandEncoding(String string) {
        string.replace('&amp;', '&')
    }

}
