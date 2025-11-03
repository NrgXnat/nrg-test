package org.nrg.testing.listeners.interceptors.filters

import groovy.util.logging.Log4j
import io.restassured.http.Method
import io.restassured.response.Response
import org.apache.commons.lang3.StringUtils
import org.nrg.testing.TestNgUtils
import org.nrg.testing.annotations.IgnoreIf
import org.nrg.testing.xnat.conf.Settings
import org.nrg.xnat.interfaces.XnatInterface
import org.testng.IMethodInstance

import java.util.regex.Pattern

@Log4j
class RestConfigurationTestFilter extends TestFilterInterceptor {

    private Map<String, String> cache = [:]
    private XnatInterface xnat = null

    RestConfigurationTestFilter() {
        super()
    }

    @Override
    boolean isTestAllowed(IMethodInstance testInstance) {
        final IgnoreIf classIgnoreIf = testInstance.method.realClass.getAnnotation(IgnoreIf) as IgnoreIf
        final IgnoreIf methodIgnoreIf = TestNgUtils.getAnnotation(testInstance.method, IgnoreIf)

        if (!classIgnoreIf && !methodIgnoreIf) {
            log.info("Running test ${TestNgUtils.getTestName(testInstance)} because neither class- nor method-level @IgnoreIf conditions exist.")
            return true
        }
        if (!classIgnoreIf) {
            log.debug("Test ${TestNgUtils.getTestName(testInstance)} has no class-level @IgnoreIf condition.")
        } else if (matchesIgnoreIf(classIgnoreIf)) {
            log.info("Test ${TestNgUtils.getTestName(testInstance)} is being ignored due to class-level @IgnoreIf condition.")
            return false
        }
        if (!methodIgnoreIf) {
            log.debug("Test ${TestNgUtils.getTestName(testInstance)} has no method-level @IgnoreIf condition.")
        } else if (matchesIgnoreIf(methodIgnoreIf)) {
            log.info("Test ${TestNgUtils.getTestName(testInstance)} is being ignored due to method-level @IgnoreIf condition.")
            return false
        }
        log.info("Running test ${TestNgUtils.getTestName(testInstance)} because class- and method-level @IgnoreIf conditions didn't exist or returned true.")
        return true
    }

    @Override
    boolean isActive() {
        true
    }

    /**
     * Evaluates whether the target XNAT matches the given @IgnoreIf annotation. This returns <pre>true</pre> if the
     * evaluation matches (i.e. the test should <i>not</i> be run) and <pre>false</pre> if it doesn't match (i.e. the
     * test should be run).
     *
     * @param ignoreIf The {@link IgnoreIf @IgnoreIf} annotation to evaluate.
     *
     * @return True if the test should be run, false if it should be ignored.
     */
    boolean matchesIgnoreIf(IgnoreIf ignoreIf) {
        final String[] values
        final String[] regexes
        if (ignoreIf.values().length == 0 && ignoreIf.regexes().length == 0) {
            log.info("No values or regexes specified in @IgnoreIf annotation. Presuming you're just checking for true or false.")
            values = ["true"]
            regexes = []
        } else {
            values = ignoreIf.values()
            regexes = ignoreIf.regexes()
        }

        final String responseBody
        try {
            responseBody = getRestValue(ignoreIf)
        } catch (RuntimeException e) {
            log.warn("Ignoring this annotation, error while evaluating @IgnoreIf annotation", e)
            return false
        }

        final boolean matchedAny
        if (matchesAnyValue(responseBody, values)) {
            log.info("Response body from REST endpoint '${ignoreIf.rest()}' matched one of the specified values.")
            matchedAny = true
        } else if (matchesAnyRegex(responseBody, regexes)) {
            log.info("Response body from REST endpoint '${ignoreIf.rest()}' matched one of the specified regular expressions.")
            matchedAny = true
        } else {
            matchedAny = false
        }

        if (ignoreIf.not()) {
            log.info("Inverting match result due to 'not' being set to true in @IgnoreIf annotation.")
            !matchedAny
        } else {
            matchedAny
        }
    }

    String getRestValue(IgnoreIf ignoreIf) {
        final String rest = ignoreIf.rest()
        final Method method = ignoreIf.method()
        final String body = ignoreIf.body()
        final String cacheKey = ignoreIf.cacheable() ? getCacheKey(rest, method, body) : null

        if (cacheKey) {
            final String cachedResult = cache[cacheKey]
            if (cachedResult) {
                log.debug("Using cached result for @IgnoreIf with key '${cacheKey}'.")
                return cachedResult
            }
        }

        final XnatInterface xnat = getInterface()
        final String url = xnat.formatXnatUrl(rest)
        final Response response = method != Method.GET && StringUtils.isNotBlank(body)
                ? xnat.queryBase().body(body).contentType(ignoreIf.contentType()).request(method, url)
                : xnat.queryBase().request(method, url)
        if (response.statusCode() >= 300) {
            throw new RuntimeException("Received HTTP ${response.statusCode()} from REST endpoint '${url}'. Expected a successful response (2xx). Ignoring this @IgnoreIf annotation.")
        }

        cacheResult(cacheKey, response.body().asPrettyString())
    }

    boolean cacheResult(String key, String result) {
        if (key) {
            cache[key] = result
        }
        result
    }

    XnatInterface getInterface() {
        if (!xnat) {
            xnat = XnatInterface.authenticate(Settings.BASEURL, Settings.DEFAULT_XNAT_CONFIG.adminUser)
        }
        xnat
    }

    private static String getCacheKey(String rest, Method method, String body) {
        final boolean hasBody = StringUtils.isNotBlank(body)
        if (method == Method.GET) {
            if (hasBody) {
                log.warn("Body specified in @IgnoreIf annotation with GET method. Ignoring body.")
            }
            rest
        } else if (!hasBody) {
            "${rest}:${method}"
        } else {
            "${rest}:${method}:${body.hashCode()}"
        }
    }

    static boolean matchesAnyValue(String response, String[] values) {
        values.contains(response)
    }

    static boolean matchesAnyRegex(String input, String[] regexes) {
        regexes?.any { String regex ->
            if (!regex) {
                return false
            }
            Pattern.compile(regex).matcher(input).matches()
        } ?: false
    }

}