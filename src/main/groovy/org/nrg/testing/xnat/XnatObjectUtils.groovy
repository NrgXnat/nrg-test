package org.nrg.testing.xnat

import org.nrg.testing.xnat.conf.Settings
import org.nrg.xnat.enums.DicomEditVersion
import org.nrg.xnat.pogo.AnonScript
import org.nrg.xnat.pogo.extensions.anon.AnonScriptFileExtension
import org.nrg.xnat.pogo.extensions.anon.AnonScriptFromUrlExtension
import org.nrg.xnat.pogo.users.User

import java.nio.file.Paths

class XnatObjectUtils {

    static AnonScript anonScriptFromFile(DicomEditVersion version, String scriptFileName) {
        anonScriptFromFile(
                version,
                new File(scriptFileName).exists() ? new File(scriptFileName) : Paths.get(Settings.DATA_LOCATION, 'anon_scripts', version.name(), scriptFileName).toFile()
        )
    }

    static AnonScript anonScriptFromFile(DicomEditVersion version, File scriptFile) {
        final AnonScript script = new AnonScript().version(version)
        script.extension(new AnonScriptFileExtension(script, scriptFile))
    }

    @SuppressWarnings('GroovyResultOfObjectAllocationIgnored')
    static AnonScript anonScriptFromURL(DicomEditVersion version, String url, User user) {
        final AnonScript script = new AnonScript().version(version)
        new AnonScriptFromUrlExtension(script, url, user)
        script
    }

}
