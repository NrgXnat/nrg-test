package org.nrg.testing.listeners.alterers

import groovy.util.logging.Log4j
import org.testng.IAlterSuiteListener
import org.testng.xml.XmlSuite

@Log4j
class NrgSuiteListener implements IAlterSuiteListener {

    @Override
    void alter(List<XmlSuite> suites) {
        log.debug('Test suite intercepted in NrgSuiteListener to set critical flags for suite.')
        suites[0].setPreserveOrder('false')
        suites[0].setConfigFailurePolicy('continue')
    }

}
