package org.nrg.testing.xnat.versions;

import org.nrg.testing.annotations.Follows;
import org.nrg.testing.xnat.conf.Settings;

public class XnatVersionLineage {

    private static boolean recursiveVersionSearch(Class<? extends XnatVersion> current, Class<? extends XnatVersion> desired) {
        if (current.getAnnotation(Follows.class) == null) return false;
        for (Class<? extends XnatVersion> version : current.getAnnotation(Follows.class).value()) {
            if (version.equals(desired)) return true;
            if (recursiveVersionSearch(version, desired)) return true;
        }
        return false;
    }

    public static boolean testedVersionFollows(Class<? extends XnatVersion> specifiedVersion) {
        return recursiveVersionSearch(Settings.XNAT_VERSION, specifiedVersion);
    }

    public static boolean testedVersionProcedes(Class<? extends XnatVersion> specifiedVersion) {
        return recursiveVersionSearch(specifiedVersion, Settings.XNAT_VERSION);
    }

}
