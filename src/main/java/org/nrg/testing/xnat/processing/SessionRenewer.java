package org.nrg.testing.xnat.processing;

public interface SessionRenewer {

    void startTimer();

    void checkAndRenewTimer();

}
