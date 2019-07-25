package org.nrg.testing


import org.testng.IMethodInstance
import org.testng.IMethodInterceptor
import org.testng.ITestContext

class UnitTestFilter implements IMethodInterceptor {

    public static final List<IMethodInstance> METHOD_INSTANCES = []

    @Override
    List<IMethodInstance> intercept(List<IMethodInstance> list, ITestContext iTestContext) {
        METHOD_INSTANCES.addAll(list)
        list
    }

    static IMethodInstance getInstance(String testName) {
        METHOD_INSTANCES.find { methodInstance ->
            TestNgUtils.getTestName(methodInstance.getMethod()) == testName
        }
    }

    static List<IMethodInstance> getDummyTests(int id) {
        METHOD_INSTANCES.findAll { instance ->
            final UnitTestId testId = TestNgUtils.getAnnotation(instance.getMethod(), UnitTestId.class)
            testId != null && testId.value() == id
        }
    }

}
