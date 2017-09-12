package org.nrg.testing.xnat.versions;

import com.google.common.base.Joiner;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.apache.log4j.Logger;
import org.nrg.testing.xnat.rest.XnatRestDriver;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XnatVersionList {

    public static final List<String> KNOWN_VERSION_KEYS = new ArrayList<>();
    public static final BiMap<String, Class<? extends XnatVersion>> KNOWN_KEY_VERSION_MAP = HashBiMap.create();
    public static final Map<Class<? extends XnatVersion>, Class<? extends XnatRestDriver>> KNOWN_VERSION_CLASS_REST_DRIVER_MAP = new HashMap<>();

    private static <T> T instantiate(Class<T> tClass) {
        try {
            return tClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Could not construct XNAT class due to:", e);
        }
    }

    public static String getKey(Class<? extends XnatVersion> xnatClass) {
        return instantiate(xnatClass).getVersionKey();
    }

    public static void readXnatVersions() {
        if (KNOWN_KEY_VERSION_MAP.isEmpty()) {
            for (Class<? extends XnatVersion> versionClass : new Reflections("org.nrg.testing.xnat.versions").getSubTypesOf(XnatVersion.class)) {
                final String versionKey = getKey(versionClass);
                KNOWN_KEY_VERSION_MAP.put(versionKey, versionClass);
            }
            for (Class<? extends XnatRestDriver> restClass : new Reflections("org.nrg.testing.xnat.rest").getSubTypesOf(XnatRestDriver.class)) {
                for (Class<? extends XnatVersion> versionClass : instantiate(restClass).getHandledVersions()) {
                    KNOWN_VERSION_CLASS_REST_DRIVER_MAP.put(versionClass, restClass);
                }
            }
            KNOWN_VERSION_KEYS.addAll(KNOWN_KEY_VERSION_MAP.keySet());
            Logger.getLogger(XnatVersionList.class).info("Known XNAT versions at runtime: " + Joiner.on(", ").join(KNOWN_VERSION_KEYS));
        }
    }

}
