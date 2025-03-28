package org.nrg.testing.xnat.performance.reset.plugin

class InternalComponentInstaller implements XnatComponentInstaller {

    @Override
    List<String> commandsForPluginById(String pluginName) {
        [
                "aws s3 cp s3://xnat-app-deployment/performance-tests/${pluginName} /home/xnat/plugins"
        ]
    }

    @Override
    List<String> commandsForPluginByUrl(String pluginName, String pluginUrl) {
        [
                "curl -L ${pluginUrl} > /home/xnat/plugins/${pluginName}.jar"
        ]
    }

    @Override
    List<String> commandsForWarByUrl(String warUrl) {
        [
                'rm -rf /home/xnat/tomcat/webapps/ROOT*',
                "curl -L ${warUrl} > /home/xnat/tomcat/webapps/ROOT.war"
        ]
    }
    
}
