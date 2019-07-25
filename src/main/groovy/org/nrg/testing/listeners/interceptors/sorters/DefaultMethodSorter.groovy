package org.nrg.testing.listeners.interceptors.sorters

import org.apache.log4j.Logger
import org.testng.IMethodInstance
import org.testng.IMethodInterceptor
import org.testng.ITestContext

class DefaultMethodSorter implements IMethodInterceptor {

    public static boolean disabled = false // Communication between IMethodInterceptors is not really allowed by TestNG, so have to use a static here to disabled default sorting if a more targeted sorter wants to take the job (pipeline tests)
    private static final Logger LOGGER = Logger.getLogger(DefaultMethodSorter)

    static void disable() {
        disabled = true
    }

    /**
     * Orders TestNG IMethodInstance methods by class, then by interdependency, and then by line number
     * @param  methods all methods being run
     * @return ordered list of method instances
     */
    List<IMethodInstance> orderMethods(List<IMethodInstance> methods) {
        final DefaultMethodSortComparator sortComparator = new DefaultMethodSortComparator(methods)
        methods.sort(false, sortComparator)
    }

    @Override
    List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
        if (disabled) {
            LOGGER.debug("More targeted test sorter detected. Skipping ordering from ${this.class.simpleName}")
            methods
        } else {
            LOGGER.debug("Method instance ordering intercepted in ${this.class.simpleName}")
            orderMethods(methods)
        }
    }

}