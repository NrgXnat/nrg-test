package org.nrg.testing.listeners.alterers

import org.apache.log4j.Logger
import org.testng.IAlterSuiteListener
import org.testng.xml.XmlSuite

class DisableOrderListener implements IAlterSuiteListener {

    @Override
    void alter(List<XmlSuite> suites) {
        Logger.getLogger(DisableOrderListener).debug('Test suite intercepted in DisableOrderListener to turn preserve-order to false.')
        suites[0].setPreserveOrder('false')
    }

}
