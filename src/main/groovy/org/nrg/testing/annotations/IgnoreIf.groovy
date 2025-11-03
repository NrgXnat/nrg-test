package org.nrg.testing.annotations

import io.restassured.http.ContentType
import io.restassured.http.Method

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target
/**
 * Allows specifying a condition under which a test method or class should be ignored.
 * The {@link IgnoreIf#rest} parameter can be used to specify a REST endpoint to be called.
 * The response is compared to the values specified in {@link IgnoreIf#values} or the regular
 * expressions specified in {@link IgnoreIf#regexes}. If one of the values or regular
 * expressions matches the return value, the test is ignored. If you don't specify at least
 * one value or regex, the evaluation is tested as a straight boolean value: if the return
 * value is <pre>true</pre>, the test is ignored;  if the return value is <pre>false</pre>,
 * the test is run. If {@link IgnoreIf#not} is set to true, the logic is inverted (i.e., the
 * test is ignored if no match is found or the return is <pre>false</pre>).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.METHOD, ElementType.TYPE])
@interface IgnoreIf {
    /**
     * The REST endpoint to call to determine if the test should be ignored.
     * @return The REST endpoint.
     */
    String rest()

    /**
     * The HTTP method to use when calling the REST endpoint. This defaults to GET and must
     * be one of GET, POST, or PUT (DELETE, HEAD, and other methods will be ignored).
     * @return The HTTP method.
     */
    Method method() default Method.GET

    /**
     * The body to use when making the REST call. Note that this is only useful for POST and PUT
     * methods and can't use any dynamic values (i.e. no property or variable substitution is
     * supported).
     * @return The body to use in the REST call.
     */
    String body() default ""

    /**
     * The content type to use when making the REST call. This defaults to application/json and
     * is only useful for POST and PUT methods.
     * @return The content type.
     */
    ContentType contentType() default ContentType.JSON

    /**
     * One or more values to compare the REST response against.
     * @return One or more values to compare with the REST response.
     */
    String[] values() default []

    /**
     * One or more regular expressions to compare the REST response against.
     * @return One or more regular expressions to compare with the REST response.
     */
    String[] regexes() default []

    /**
     * If true, inverts the logic of the matching (i.e., ignores the test if no match is found).
     * @return True to invert the logic, false otherwise.
     */
    boolean not() default false

    /**
     * If true, indicates that the result of this annotation can be cached for the duration of
     * the test run. By default, this is true, since tests are not expected to change the conditions
     * that would affect the outcome of this annotation. However, if a test does change such conditions,
     * this should be set to false to ensure accurate evaluation.
     * @return Whether the result can be cached.
     */
    boolean cacheable() default true
}
