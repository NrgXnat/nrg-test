package org.nrg.testing.xnat.ssh

class SystemctlTomcatController extends CentralizableTomcatController {

    @Override
    String getCommandString(String command) {
        "sudo systemctl ${command} tomcat"
    }

}
