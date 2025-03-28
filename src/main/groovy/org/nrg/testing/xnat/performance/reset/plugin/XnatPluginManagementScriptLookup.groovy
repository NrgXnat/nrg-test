package org.nrg.testing.xnat.performance.reset.plugin

class XnatPluginManagementScriptLookup {

    private static final String DEFAULT = 'internal'

    private static final Map<String, XnatComponentInstaller> INSTALL_SCRIPTS = [
            (DEFAULT): new InternalComponentInstaller()
    ]

    private static final Map<String, XnatComponentUninstaller> UNINSTALL_SCRIPTS = [
            (DEFAULT): new InternalComponentUninstaller()
    ]

    static XnatComponentInstaller lookupInstaller(String managerKey) {
        INSTALL_SCRIPTS.get(managerKey) ?: INSTALL_SCRIPTS[DEFAULT]
    }

    static XnatComponentUninstaller lookupUninstaller(String managerKey) {
        UNINSTALL_SCRIPTS.get(managerKey) ?: UNINSTALL_SCRIPTS[DEFAULT]
    }

}
