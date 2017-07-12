/*
 * org.nrg.selenium.util.BaseProperties
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.testing.util;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BaseProperties {

    private static final Logger LOGGER = Logger.getLogger(BaseProperties.class);
    private static final String XNAT_CONFIG_FOLDER = "/config/";
    protected String configProperty;
    protected String defaultConfig;
    protected Properties properties;

    public BaseProperties(String configProperty, String defaultConfig) {
        this.configProperty = configProperty;
        this.defaultConfig = defaultConfig;
    }

    /*
     * On the first invocation of getProperties, the method will search for a
     * property file on the classpath at /config/<xnat.config>, where
     * xnat.config is a system property. The property file is then cached for
     * subsequent use.
     */
    protected synchronized Properties getProperties() {
        if (properties == null) {
            String config = System.getProperty(configProperty);
            if (config == null || "${xnat.config}".equals(config)) {
                config = defaultConfig;
                LOGGER.debug(configProperty + " variable not specified, using default, " + defaultConfig);
            }

            try {
                properties = new Properties();
                final String configPath = XNAT_CONFIG_FOLDER + config;
                InputStream configStream = this.getClass().getResourceAsStream(configPath);
                if (configStream == null){
                    throw new RuntimeException("Config file, " + configPath + ", not found");
                }
                properties.load(configStream);
                LOGGER.debug("Loaded properties from classpath location, " + configPath);
                return properties;
            } catch (IOException e) {
                throw new RuntimeException("Error obtaining config file: " + config, e);
            }
        } else {
            return properties;
        }
    }

    protected String getSensitiveProperty(String property) {
        if (getProperty(property) != null) {
            LOGGER.warn(property + " is included in properties file!");
        }
        return getPropertyFromAnywhere(property);
    }

    protected String getPropertyFromAnywhere(String property) {
        if (getCommandLineArgument(property) != null) {
            return getCommandLineArgument(property);
            // If command line argument is provided, return that.
        }
        // ... otherwise, return what's in the properties file.
        return getProperty(property);
    }

    protected String getCommandLineArgument(String property) {
        return System.getProperty(property);
    }

    protected String getProperty(String property) {
        return getProperties().getProperty(property);
    }

    protected String getStringProperty(boolean isSensitive, String property, String defaultValue) {
        if (getPropertyFromAnywhere(property) == null) {
            return defaultValue;
        }
        if (isSensitive) return getSensitiveProperty(property);
        return getPropertyFromAnywhere(property);
    }

    protected boolean getBooleanProperty(String property, boolean defaultValue) {
        if (getPropertyFromAnywhere(property) == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(getPropertyFromAnywhere(property));
    }

    protected int getIntProperty(String property, int defaultValue) {
        if (getPropertyFromAnywhere(property) == null) {
            return defaultValue;
        }
        return Integer.parseInt(getPropertyFromAnywhere(property));
    }
}
