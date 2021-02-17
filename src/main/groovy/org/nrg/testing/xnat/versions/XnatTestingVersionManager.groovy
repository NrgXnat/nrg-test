package org.nrg.testing.xnat.versions

import groovy.util.logging.Log4j

import org.nrg.testing.xnat.conf.Settings
import org.nrg.testing.xnat.rest.XnatRestDriver
import org.nrg.xnat.versions.XnatVersion
import org.nrg.xnat.versions.XnatVersionList
import org.reflections.Reflections

@Log4j
class XnatTestingVersionManager {

    public static final Map<Class<? extends XnatVersion>, Class<? extends XnatRestDriver>> KNOWN_VERSION_CLASS_REST_DRIVER_MAP = [:]

    static {
        new Reflections('org.nrg.testing.xnat.rest').getSubTypesOf(XnatRestDriver).each { driverClass ->
            driverClass.newInstance().handledVersions.each { xnatVersionClass ->
                KNOWN_VERSION_CLASS_REST_DRIVER_MAP.put(xnatVersionClass, driverClass)
            }
        }
    }

    static boolean testedVersionPrecedes(Class<? extends XnatVersion> specifiedVersion) {
        XnatVersionList.firstFollowsSecond(specifiedVersion, Settings.XNAT_VERSION)
    }

    static boolean testedVersionFollows(Class<? extends XnatVersion> specifiedVersion) {
        XnatVersionList.firstFollowsSecond(Settings.XNAT_VERSION, specifiedVersion)
    }

    static Class<? extends XnatRestDriver> lookupDriverClass(Class<? extends XnatVersion> xnatVersionClass) {
        KNOWN_VERSION_CLASS_REST_DRIVER_MAP[xnatVersionClass]
    }

}
