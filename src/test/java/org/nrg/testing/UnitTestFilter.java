package org.nrg.testing;

import org.nrg.testing.util.TestNgUtils;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;

import java.util.ArrayList;
import java.util.List;

public class UnitTestFilter implements IMethodInterceptor {

    public static final List<IMethodInstance> METHOD_INSTANCES = new ArrayList<>();

    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> list, ITestContext iTestContext) {
        METHOD_INSTANCES.addAll(list);
        return list;
    }

    public static IMethodInstance getInstance(String testName) {
        for (IMethodInstance methodInstance : METHOD_INSTANCES) {
            if (TestNgUtils.getTestName(methodInstance.getMethod()).equals(testName)) {
                return methodInstance;
            }
        }
        return null;
    }

    public static List<IMethodInstance> getDummyTests(int id) {
        final List<IMethodInstance> instances = new ArrayList<>();
        for (IMethodInstance instance : METHOD_INSTANCES) {
            final UnitTestId testId = TestNgUtils.getAnnotation(instance.getMethod(), UnitTestId.class);
            if (testId != null && testId.value() == id) {
                instances.add(instance);
            }
        }
        return instances;
    }

}
