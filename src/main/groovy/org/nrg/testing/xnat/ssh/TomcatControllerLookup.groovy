package org.nrg.testing.xnat.ssh

import groovy.util.logging.Log4j

@Log4j
class TomcatControllerLookup {

    private static final Map<String, TomcatController> CONTROLLERS = [
            'service-tomcat': new ServiceTomcatController('tomcat'),
            'service-tomcat7': new ServiceTomcatController('tomcat7'),
            'service-tomcat8': new ServiceTomcatController('tomcat8'),
            'service-tomcat9': new ServiceTomcatController('tomcat9')
    ]

    static TomcatController lookup(String managerKey) {
        CONTROLLERS.get(managerKey)
    }

}
