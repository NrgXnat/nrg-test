package org.nrg.testing.xnat.ssh

class DockerComposeTomcatController extends CentralizableTomcatController {

    @Override
    String getCommandString(String command) {
        "cd /data.local/xnatdev/automated_testing/xnat-docker-compose; docker-compose ${command} xnat-web"
    }

    @Override
    String getStartCommand() {
        getRestartCommand()
    }

}
