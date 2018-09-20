package org.nrg.testing.listeners.interceptors.filters;

import org.nrg.testing.annotations.Basic;
import org.nrg.testing.util.TestNgUtils;
import org.nrg.testing.xnat.conf.Settings;
import org.testng.IMethodInstance;

public class BasicTestFilter extends TestFilterInterceptor {

    public BasicTestFilter() {
        if (Settings.BASIC_MODE) {
            TestFilterInterceptors.registerListener(this);
        }
    }

    @Override
    public boolean isTestAllowed(IMethodInstance testInstance) {
        final Class<?> classObj = testInstance.getMethod().getRealClass();
        return TestNgUtils.getAnnotation(testInstance.getMethod(), Basic.class) != null || classObj.getAnnotation(Basic.class) != null;
    }

    @Override
    public boolean isActive() {
        return Settings.BASIC_MODE;
    }

}