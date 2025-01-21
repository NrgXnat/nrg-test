package org.nrg.testing.xnat.performance.reset

class InternalXnatReset extends FixedScriptXnatReset {

    InternalXnatReset(boolean useSsh) {
        super(useSsh)
    }

    @Override
    List<String> commands() {
        [
                'sudo systemctl stop tomcat',
                'dropdb xnat -U xnat',
                'sudo rm -rf /opt/data/archive/* /opt/data/build/* /opt/data/cache/* /opt/data/prearchive/*',
                'createdb xnat -U xnat',
                'curl -u admin:admin "http://localhost:8161/api/jolokia/search/*:destinationType=Queue,*" | jq -r \'.value | map(split(",")) | flatten | map(select(contains("destinationName"))) | map(split("=")) | map(.[1]) | unique | .[]\' | while read queue; do curl -u admin:admin "http://localhost:8161/api/jolokia/exec/org.apache.activemq:brokerName=localhost,destinationName=${queue},destinationType=Queue,type=Broker/purge()"; done',
                'sudo systemctl start tomcat'
        ]
    }

}
