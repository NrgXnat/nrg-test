package org.nrg.testing.email;

import javax.mail.BodyPart;

public class EmailAttachment {

    private String fileName;
    private Object contents;
    private String contentType;

    public EmailAttachment(BodyPart attachment) {
        try {
            this.fileName = attachment.getFileName();
            this.contents = attachment.getContent();
            this.contentType = attachment.getContentType();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read attachment from email", e);
        }
    }

    public String getFileName() {
        return fileName;
    }

    public Object getContents() {
        return contents;
    }

    public String getStringContents() {
        if (contentType.toLowerCase().contains("text/plain")) return (String)contents;
        return null;
    }
}
