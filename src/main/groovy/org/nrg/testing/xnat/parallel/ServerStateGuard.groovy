package org.nrg.testing.xnat.parallel

import groovy.util.logging.Log4j
import io.restassured.RestAssured
import io.restassured.filter.Filter
import io.restassured.filter.FilterContext
import io.restassured.response.Response
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.FilterableResponseSpecification
import org.apache.commons.lang3.StringUtils
import org.nrg.testing.xnat.conf.Settings

import java.util.regex.Pattern

/**
 * Fails fast when a test class that is not marked @MutatesServerState issues an HTTP request that
 * mutates server-global XNAT state. Installed as a global REST-assured filter when isolation is
 * enforced (xnat.parallel.enforceIsolation, which defaults to on whenever xnat.parallel.classes > 1),
 * so it sees every request the framework or the tests build through REST-assured.
 *
 * Requests issued outside any test class context (e.g. @BeforeSuite XNAT initialization and user
 * setup) are always allowed. The guard is an enforcement net for the serial/parallel classification,
 * turning a quiet cross-class state leak into an immediate, attributable failure.
 */
@Log4j
class ServerStateGuard implements Filter {

    private static final List<String> MUTATING_METHODS = ['POST', 'PUT', 'DELETE', 'PATCH']
    private static volatile boolean installed = false

    static void install() {
        synchronized (ServerStateGuard) {
            if (!installed) {
                RestAssured.filters(new ServerStateGuard())
                installed = true
                log.info('Server state guard installed: classes not marked @MutatesServerState may not write server-global XNAT state.')
            }
        }
    }

    @Override
    Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
        final Class testClass = IsolationManager.currentTestClass()
        if (testClass != null && !IsolationManager.mutatesServerState(testClass)) {
            final String reason = mutationReason(requestSpec.method, pathOf(requestSpec.URI), sharedUsernames())
            if (reason != null) {
                throw new IllegalStateException("Test class ${testClass.simpleName} issued ${requestSpec.method} ${requestSpec.URI}, " +
                        "which mutates server-global XNAT state (${reason}), but the class is not annotated @MutatesServerState. " +
                        'Annotate the class so it is serialized against the rest of the suite, or scope the call to a project.')
            }
        }
        ctx.next(requestSpec, responseSpec)
    }

    /**
     * Pure classification of a request against the known server-global mutation surface.
     * @return a short human-readable reason if the request is a server-global mutation, else null
     */
    static String mutationReason(String httpMethod, String path, Collection<String> protectedUsernames) {
        if (!(StringUtils.upperCase(httpMethod) in MUTATING_METHODS) || path == null) {
            return null
        }
        if (path.contains('/xapi/siteConfig')) {
            return 'site config write'
        }
        if (path.contains('/data/config') && !path.contains('/projects/')) {
            return 'site-level config service write (anon script, DICOM routing rules, etc.)'
        }
        final String sharedUser = protectedUsernames.find { username ->
            StringUtils.isNotBlank(username) && path =~ /\/(?:xapi|data)\/users?\/${Pattern.quote(username)}(?:\/|$)/
        }
        if (sharedUser != null) {
            return "mutation of shared user account '${sharedUser}'"
        }
        null
    }

    private static Collection<String> sharedUsernames() {
        [Settings.MAIN_USERNAME, Settings.MAIN_ADMIN_USERNAME, Settings.ADMIN_USERNAME, 'guest'].findAll()
    }

    private static String pathOf(String uri) {
        try {
            URI.create(uri).path
        } catch (Exception ignored) {
            uri // fall back to matching against the raw string
        }
    }

}
