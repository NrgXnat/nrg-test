package org.nrg.testing.email

import com.google.api.services.gmail.model.Message

import java.util.concurrent.TimeUnit
import java.util.function.Predicate

import static org.awaitility.Awaitility.await

class EmailQuery {

    private static long suiteStartTime
    private static final int DEFAULT_TIMEOUT_SECONDS = 300

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

    EmailQueryResult repeatQueryUntilArbitraryCondition(Predicate<List<Message>> condition, int timeout = DEFAULT_TIMEOUT_SECONDS) {
        new EmailQueryResult(
                await().atMost(timeout, TimeUnit.SECONDS).until(
                        () -> EmailClient.queryGmail(buildQuery()),
                        condition
                )
        )
    }

    EmailQueryResult issueQueryOnce(int timeout = DEFAULT_TIMEOUT_SECONDS) {
        repeatQueryUntilArbitraryCondition(messages -> true, timeout)
    }

    EmailQueryResult queryUntilMatchingExactNumberOfResults(int expectedResults, int timeout = DEFAULT_TIMEOUT_SECONDS) {
        repeatQueryUntilArbitraryCondition(
                messages -> messages.size() == expectedResults,
                timeout
        )
    }

    EmailQueryResult queryUntilSingleResult(int timeout = DEFAULT_TIMEOUT_SECONDS) {
        queryUntilMatchingExactNumberOfResults(1, timeout)
    }

    private String buildQuery() {
        final List<String> components = ['{in:spam in:inbox}']
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
