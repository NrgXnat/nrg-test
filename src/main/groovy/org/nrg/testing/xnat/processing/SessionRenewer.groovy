package org.nrg.testing.xnat.processing

interface SessionRenewer {

    void startTimer()

    void checkAndRenewTimer()

}
