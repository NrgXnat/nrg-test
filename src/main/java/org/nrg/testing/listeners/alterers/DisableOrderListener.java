package org.nrg.testing.listeners.alterers;

import org.apache.log4j.Logger;
import org.testng.IAlterSuiteListener;
import org.testng.xml.XmlSuite;

import java.util.List;

public class DisableOrderListener implements IAlterSuiteListener {

    @Override
    public void alter(List<XmlSuite> suites) {
        Logger.getLogger(DisableOrderListener.class).debug("Test suite intercepted in DisableOrderListener to turn preserve-order to false.");
        suites.get(0).setPreserveOrder("false");
    }

}
