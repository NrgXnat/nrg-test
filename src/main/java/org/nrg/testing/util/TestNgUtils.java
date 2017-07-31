package org.nrg.testing.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.nrg.testing.BaseTestCase;
import org.nrg.testing.file.FileIO;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.SkipException;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

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

    public static void assertNonempty(CharSequence string) {
        assertTrue(StringUtils.isNotEmpty(string));
    }

    public static void assertBinaryFilesEqual(File file1, File file2) {
        try {
            assertEquals("Binary files not equal", FileIO.calculateMD5(file1), FileIO.calculateMD5(file2));
        } catch (IOException ioe) {
            fail("Could not assert files equal due to: " + ioe);
        }
    }

}
