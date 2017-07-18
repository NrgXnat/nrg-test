package org.nrg.testing.xnat.rest;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Map;

public class SerializationUtils {

    private static final TypeReference<Map<String, Object>> mapTypeRef = new TypeReference<Map<String, Object>>(){};

    public static Map<String, Object> serializeToMap(Object object) {
        try {
            return XnatRestDriver.XNAT_REST_MAPPER.readValue(XnatRestDriver.XNAT_REST_MAPPER.writeValueAsString(object), mapTypeRef);
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred in serializing object.", e);
        }
    }

}
