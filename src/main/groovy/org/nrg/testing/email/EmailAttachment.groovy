package org.nrg.testing.email

import javax.mail.BodyPart

class EmailAttachment {

    String fileName
    Object contents
    String contentType

    EmailAttachment(BodyPart attachment) {
        this.fileName = attachment.fileName
        this.contents = attachment.content
        this.contentType = attachment.contentType
    }

    String getStringContents() {
        (contentType.toLowerCase().contains('text/plain')) ? contents as String : null
    }

}
