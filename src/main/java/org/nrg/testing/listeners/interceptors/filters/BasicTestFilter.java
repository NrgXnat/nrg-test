package org.nrg.testing.listeners.interceptors.filters;

import org.apache.log4j.Logger;
import org.nrg.testing.annotations.Basic;
import org.nrg.testing.util.TestNgUtils;
import org.nrg.testing.xnat.conf.Settings;
import org.testng.IMethodInstance;
import org.testng.ITestContext;

import java.util.List;

public class BasicTestFilter extends TestFilterInterceptor {

    private static final Logger LOGGER = Logger.getLogger(BasicTestFilter.class);

    public BasicTestFilter() {
        if (Settings.BASIC_MODE) {
            TestFilterInterceptors.registerListener(this);
        }
    }

    @Override
    public boolean isTestAllowed(IMethodInstance testInstance) {
        final Class<?> classObj = testInstance.getMethod().getRealClass();
        return !Settings.BASIC_MODE || TestNgUtils.getAnnotation(testInstance.getMethod(), Basic.class) != null || classObj.getAnnotation(Basic.class) != null;
    }

    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
        if (!Settings.BASIC_MODE) {
            LOGGER.debug("Basic mode is not enabled, running all allowed tests...");
            return methods;
        }
        return super.intercept(methods, context);
    }

}