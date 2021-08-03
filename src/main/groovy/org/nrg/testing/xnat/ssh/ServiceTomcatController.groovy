package org.nrg.testing.xnat.ssh

class ServiceTomcatController extends CentralizableTomcatController {

    final String serviceName

    ServiceTomcatController(String serviceName) {
        this.serviceName = serviceName
    }

    @Override
    String getCommandString(String command) {
        "sudo service ${serviceName} ${command}"
    }

}
