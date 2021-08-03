package org.nrg.testing.xnat.ssh

interface TomcatController {

    String getStartCommand()
    String getStopCommand()
    String getRestartCommand()

}
