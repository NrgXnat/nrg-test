package org.nrg.testing.xnat.performance.reset.plugin

class InternalPluginInstaller implements XnatPluginInstaller {

    @Override
    List<String> commands(String pluginName) {
        [
                "aws s3 cp s3://xnat-app-deployment/performance-tests/${pluginName} /home/xnat/plugins"
        ]
    }

}
