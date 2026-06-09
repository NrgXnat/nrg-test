package org.nrg.testing.listeners.alterers

import groovy.util.logging.Log4j
import org.nrg.testing.xnat.conf.Settings
import org.testng.IAlterSuiteListener
import org.testng.xml.XmlSuite

@Log4j
class NrgSuiteListener implements IAlterSuiteListener {

    @Override
    void alter(List<XmlSuite> suites) {
        log.debug('Test suite intercepted in NrgSuiteListener to set critical flags for suite.')
        suites[0].setPreserveOrder('false')
        suites[0].setConfigFailurePolicy('continue')
        configureParallelism(suites[0])
    }

    private static void configureParallelism(XmlSuite suite) {
        final int parallelClasses
        try {
            parallelClasses = Settings.PARALLEL_CLASSES
        } catch (Throwable ignored) {
            // Settings can't initialize outside a configured XNAT test environment (e.g. this
            // framework's own unit tests); parallelism only applies to XNAT suites anyway
            return
        }
        if (parallelClasses > 1) {
            suite.setParallel(XmlSuite.ParallelMode.CLASSES)
            suite.setThreadCount(parallelClasses)
            log.info("Parallel class execution enabled: up to ${parallelClasses} test classes at a time. " +
                    'Classes marked @MutatesServerState are serialized against the whole suite.')
        }
    }

}
