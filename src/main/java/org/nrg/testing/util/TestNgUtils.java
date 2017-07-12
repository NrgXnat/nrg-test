package org.nrg.testing.util;

import org.apache.log4j.Logger;
import org.nrg.testing.BaseTestCase;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.SkipException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class TestNgUtils {

    private static final Logger LOGGER = Logger.getLogger(TestNgUtils.class);

    public static String getTestName(ITestResult result) {
        return result.getMethod().getMethodName();
    }

    public static Class getTestClass(ITestResult result) {
        return result.getMethod().getRealClass();
    }

    public static String getTestClassName(ITestResult result) {
        return getTestClass(result).getSimpleName();
    }

    public static String getTestName(ITestNGMethod method) {
        return method.getMethodName();
    }

    public static Class getTestClass(ITestNGMethod method) {
        return method.getRealClass();
    }

    public static String getTestClassName(ITestNGMethod method) {
        return getTestClass(method).getSimpleName();
    }

    public static boolean testFailed(ITestResult result) {
        return result.getStatus() == ITestResult.FAILURE;
    }

    public static boolean checkTestFailingAndEquals(ITestResult result, String test) {
        return test.equals(getTestName(result)) && testFailed(result);
    }

    public static <T extends Annotation> T getAnnotation(ITestNGMethod test, Class<T> annotation) {
        return test.getConstructorOrMethod().getMethod().getAnnotation(annotation);
    }

    public static ITestNGMethod getTestByMethod(Method method) {
        for (ITestNGMethod test : BaseTestCase.getAllTests()) {
            if (test.getConstructorOrMethod().getMethod().equals(method)) {
                return test;
            }
        }
        return null;
    }

    public static void assumeTrue(boolean condition, String message) {
        if (condition) return;
        LOGGER.warn(message);
        throw new SkipException(message);
    }

    public static void assumeFalse(boolean condition, String message) {
        assumeTrue(!condition, message);
    }

}
