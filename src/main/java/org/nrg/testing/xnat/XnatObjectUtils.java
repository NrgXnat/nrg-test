package org.nrg.testing.xnat;

import org.nrg.testing.xnat.conf.Settings;
import org.nrg.xnat.enums.DicomEditVersion;
import org.nrg.xnat.pogo.AnonScript;
import org.nrg.xnat.pogo.extensions.anon.AnonScriptFileExtension;
import org.nrg.xnat.pogo.extensions.anon.AnonScriptFromUrlExtension;
import org.nrg.xnat.pogo.users.User;

import java.io.File;
import java.nio.file.Paths;

public class XnatObjectUtils {

    public static AnonScript anonScriptFromFile(DicomEditVersion version, String scriptFileName) {
        return anonScriptFromFile(version, new File(scriptFileName).exists() ? new File(scriptFileName) : Paths.get(Settings.DATA_LOCATION, "anon_scripts", version.name(), scriptFileName).toFile());
    }

    public static AnonScript anonScriptFromFile(DicomEditVersion version, File scriptFile) {
        final AnonScript script = new AnonScript().version(version);
        return script.extension(new AnonScriptFileExtension(script, scriptFile));
    }

    public static AnonScript anonScriptFromURL(DicomEditVersion version, String url, User user) {
        final AnonScript script = new AnonScript().version(version);
        new AnonScriptFromUrlExtension(script, url, user);
        return script;
    }

}
