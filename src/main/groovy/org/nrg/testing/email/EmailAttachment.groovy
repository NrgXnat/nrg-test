package org.nrg.testing.email

class EmailAttachment {

    String fileName
    Object contents
    String contentType

    String getStringContents() {
        (contentType.toLowerCase().contains('text/plain')) ? contents as String : null
    }

    void setBase64UrlContents(String base64Url) {
        contents = new String(base64Url.decodeBase64Url())
    }

}
