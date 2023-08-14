package org.nrg.testing.email

import com.google.api.services.gmail.model.Message
import org.nrg.testing.xnat.conf.Settings

import java.util.concurrent.TimeUnit
import java.util.function.Predicate

import static org.awaitility.Awaitility.await

class EmailQuery {

    private static long suiteStartTime

    List<String> requiredStrings = []
    List<String> forbiddenStrings = []
    Long sinceEpochSeconds

    EmailQuery containing(List<String> containedStrings) {
        requiredStrings.addAll(containedStrings)
        this
    }

    EmailQuery containing(String containedString) {
        containing([containedString])
    }

    EmailQuery notContaining(List<String> forbiddenStrings) {
        forbiddenStrings.addAll(forbiddenStrings)
        this
    }

    EmailQuery notContaining(String forbiddenString) {
        notContaining([forbiddenString])
    }

    EmailQuery sinceEpochSeconds(long time) {
        sinceEpochSeconds = time
        this
    }

    EmailQuery since(Date time) {
        sinceEpochSeconds(time.time / 1000 as long)
    }

    EmailQuery fromCatalog(XnatEmailCatalog emailCatalog) {
        emailCatalog.define(this)
        this
    }

    EmailQueryResult repeatQueryUntilArbitraryCondition(Predicate<List<Message>> condition, int timeout = Settings.DEFAULT_TIMEOUT) {
        new EmailQueryResult(
                await().atMost(timeout, TimeUnit.SECONDS).until(
                        () -> EmailClient.queryGmail(buildQuery()),
                        condition
                )
        )
    }

    EmailQueryResult issueQueryOnce(int timeout = Settings.DEFAULT_TIMEOUT) {
        repeatQueryUntilArbitraryCondition(messages -> true, timeout)
    }

    EmailQueryResult queryUntilMatchingExactNumberOfResults(int expectedResults, int timeout = Settings.DEFAULT_TIMEOUT) {
        repeatQueryUntilArbitraryCondition(
                messages -> messages.size() == expectedResults,
                timeout
        )
    }

    EmailQueryResult queryUntilSingleResult(int timeout = Settings.DEFAULT_TIMEOUT) {
        queryUntilMatchingExactNumberOfResults(1, timeout)
    }

    private String buildQuery() {
        final List<String> components = []
        if (requiredStrings.size() > 0) {
            components << requiredStrings.collect { requiredTerm ->
                "\"${requiredTerm}\""
            }.join(' ')
        }
        if (forbiddenStrings.size() > 0) {
            components << forbiddenStrings.collect { forbiddenTerm ->
                "-\"${forbiddenTerm}\""
            }.join(' ')
        }
        components << "after:${(sinceEpochSeconds ?: suiteStartTime) - 1}".toString() // go back 1 second to not worry about rounding errors
        components.join(' ')
    }

    static setStartTime() {
        suiteStartTime = System.currentTimeMillis() / 1000 as long
    }

}
