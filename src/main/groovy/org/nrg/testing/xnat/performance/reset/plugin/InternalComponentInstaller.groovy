package org.nrg.testing.xnat.performance.reset.plugin

class InternalComponentInstaller implements XnatComponentInstaller {

    public static final String CACHED_DOWNLOADS_DIR = '/home/xnat/downloaded_artifacts'
    public static final String CREATE_CACHE = "mkdir -p ${CACHED_DOWNLOADS_DIR}"
    private static boolean cacheCreated = false

    @Override
    List<String> commandsForPluginById(String pluginName) {
        [
                "aws s3 cp s3://xnat-app-deployment/performance-tests/${pluginName} /home/xnat/plugins"
        ]
    }

    @Override
    List<String> commandsForPluginByUrl(String pluginName, String pluginUrl) {
        final List<String> commands = initCommandList(pluginName, pluginUrl)
        commands << "cp ${CACHED_DOWNLOADS_DIR}/${pluginName} /home/xnat/plugins/${pluginName}".toString()
        commands
    }

    @Override
    List<String> commandsForWarByUrl(String warUrl) {
        final String warName = warUrl.split('/').last()
        final List<String> commands = initCommandList(warName, warUrl)
        commands << 'sudo rm -rf /home/xnat/tomcat/webapps/ROOT*'
        commands << "cp ${CACHED_DOWNLOADS_DIR}/${warName} /home/xnat/tomcat/webapps/ROOT.war".toString()
        commands
    }

    List<String> initCommandList(String artifactName, String artifactUrl) {
        final List<String> commands = []
        if (!cacheCreated) {
            commands << CREATE_CACHE
            cacheCreated = true
        }
        commands << cacheArtifact(artifactName, artifactUrl)
        commands
    }

    String cacheArtifact(String localDownloadName, String artifactUrl) {
        final String fullDownloadPath = "${CACHED_DOWNLOADS_DIR}/${localDownloadName}"
        "if [ ! -f ${fullDownloadPath} ]; then curl -L ${artifactUrl} > ${fullDownloadPath}; fi"
    }
    
}
