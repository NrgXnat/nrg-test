package org.nrg.testing.xnat.ssh

abstract class CentralizableTomcatController implements TomcatController {

    @Override
    String getStartCommand() {
        getCommandString('start')
    }

    @Override
    String getStopCommand() {
        getCommandString('stop')
    }

    @Override
    String getRestartCommand() {
        getCommandString('restart')
    }

    abstract String getCommandString(String command)

}
