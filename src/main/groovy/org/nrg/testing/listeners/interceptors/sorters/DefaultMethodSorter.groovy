package org.nrg.testing.listeners.interceptors.sorters

import groovy.util.logging.Log4j
import org.apache.log4j.Logger
import org.nrg.testing.TestNgUtils
import org.testng.IMethodInstance
import org.testng.IMethodInterceptor
import org.testng.ITestContext

@Log4j
class DefaultMethodSorter implements IMethodInterceptor {

    public static boolean disabled = false // Communication between IMethodInterceptors is not really allowed by TestNG, so have to use a static here to disabled default sorting if a more targeted sorter wants to take the job (pipeline tests)

    static void disable() {
        disabled = true
    }

    /**
     * Orders TestNG IMethodInstance methods by:
     *   1. Non-performance tests first
     *   2. ...then by class
     *   3. ...then by interdependency
     *   4. ...and then by line number
     * @param  methods all methods being run
     * @return ordered list of method instances
     */
    List<IMethodInstance> orderMethods(List<IMethodInstance> methods) {
        final DefaultMethodSortComparator sortComparator = new DefaultMethodSortComparator(methods)
        final List<IMethodInstance> sorted = methods.sort(false, sortComparator)
        log.info("Methods have been sorted by ${this.class.simpleName} as: ${sorted.collect { TestNgUtils.getTestName(it) }}")
        sorted
    }

    @Override
    List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
        if (disabled) {
            log.debug("More targeted test sorter detected. Skipping ordering from ${this.class.simpleName}")
            methods
        } else {
            log.debug("Method instance ordering intercepted in ${this.class.simpleName}")
            final List<IMethodInstance> sorted = orderMethods(methods)
            sorted
        }
    }

}