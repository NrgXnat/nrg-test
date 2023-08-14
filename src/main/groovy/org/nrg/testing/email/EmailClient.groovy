package org.nrg.testing.email

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.GmailScopes
import com.google.api.services.gmail.model.Message
import com.google.api.services.gmail.model.MessagePartBody
import groovy.util.logging.Log4j
import org.nrg.testing.xnat.conf.Settings
import org.nrg.testing.xnat.conf.XNATProperties

@Log4j
class EmailClient {

    private static final String USER = 'me'
    private static final String APPLICATION_NAME = 'automation-test'
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance()

    private static Gmail getGmailService() {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        new Gmail.Builder(httpTransport, JSON_FACTORY, getCredentials(httpTransport))
                .setApplicationName(APPLICATION_NAME)
                .build()
    }

    /**
     * Taken from: https://developers.google.com/gmail/api/quickstart/java
     */
    private static Credential getCredentials(NetHttpTransport httpTransport) {
        final InputStream input = new FileInputStream(new File(Settings.EMAIL_CREDENTIALS_JSON))
        final GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(input))

        // Build flow and trigger user authorization request.
        final GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport,
                JSON_FACTORY,
                clientSecrets,
                [GmailScopes.GMAIL_READONLY]
        )
                .setDataStoreFactory(new FileDataStoreFactory(new File(Settings.EMAIL_TOKENS_DIR)))
                .setAccessType('offline')
                .build()
        new AuthorizationCodeInstalledApp(
                flow,
                new LocalServerReceiver.Builder().setPort(8888).build()
        ).authorize('user')
    }

    static List<Message> queryGmail(String query) {
        getGmailService()
                .users()
                .messages()
                .list(USER)
                .setQ(query)
                .execute()
                .getMessages() ?: []
    }

    static Message retrieveFullMessage(String messageId) {
        getGmailService()
                .users()
                .messages()
                .get(USER, messageId)
                .setFormat('full')
                .execute()
    }

    static MessagePartBody retrieveAttachment(String messageId, String attachmentId) {
        getGmailService()
                .users()
                .messages()
                .attachments()
                .get(USER, messageId, attachmentId)
                .execute()
    }

    static boolean test() {
        if (Settings.EMAIL_CREDENTIALS_JSON == null) {
            log.info("${XNATProperties.EMAIL_CREDENTIALS} not found. Functionality for reading email will be unavailable.")
            return false
        } else if (!new File(Settings.EMAIL_CREDENTIALS_JSON).exists()) {
            log.warn("Email credentials file specified to be ${Settings.EMAIL_CREDENTIALS_JSON}, but this file could not be located.")
            return false
        }
        try {
            log.info('Email credentials file found. Attempting to connect to gmail API...')
            getGmailService()
            return true
        } catch (Throwable t) {
            log.warn('Encountered an issue when attempting to connect to gmail', t)
            return false
        }
    }

}
