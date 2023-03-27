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
                'rm -rf /opt/data/archive/* /opt/data/build/* /opt/data/cache/* /opt/data/prearchive/*',
                'createdb xnat -U xnat',
                'sudo systemctl start tomcat'
        ]
    }

}
