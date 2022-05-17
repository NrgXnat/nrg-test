package org.nrg.testing.util

class VersionParser {

    static boolean versionCompatible(String minimumVersion, String actualVersion) {
        actualVersion.split('-')[0] >= minimumVersion
    }

}
