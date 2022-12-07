package org.nrg.testing.xnat.performance.reset

class InternalXnatReset extends FixedScriptXnatReset {

    @Override
    List<String> commands() {
        [
                'systemctl stop tomcat',
                'dropdb xnat -U xnat',
                'rm -rf /opt/data/archive/* /opt/data/build/* /opt/data/cache/* /opt/data/prearchive/*',
                'createdb xnat -U xnat',
                'systemctl start tomcat'
        ]
    }

}
