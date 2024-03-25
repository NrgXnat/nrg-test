package org.nrg.testing.xnat.performance.reset.plugin

class XnatPluginManagementScriptLookup {

    private static final String DEFAULT = 'internal'

    private static final Map<String, XnatPluginInstaller> INSTALL_SCRIPTS = [
            (DEFAULT): new InternalPluginInstaller()
    ]

    private static final Map<String, XnatPluginUninstaller> UNINSTALL_SCRIPTS = [
            (DEFAULT): new InternalPluginUninstaller()
    ]

    static XnatPluginInstaller lookupInstaller(String managerKey) {
        INSTALL_SCRIPTS.get(managerKey) ?: INSTALL_SCRIPTS[DEFAULT]
    }

    static XnatPluginUninstaller lookupUninstaller(String managerKey) {
        UNINSTALL_SCRIPTS.get(managerKey) ?: UNINSTALL_SCRIPTS[DEFAULT]
    }

}
