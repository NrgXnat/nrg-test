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
     * On the first invocation, the method will search for a
     * property file on the classpath at /config/${configProperty}, where
     * ${configProperty} is a system property that defaults to ${defaultConfig}. The property file is then cached for
     * subsequent use.
     */
    protected synchronized Properties getPropertiesFromFile() {
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

    protected String getSensitiveProperty(String[] propertyAliases) {
        return getPropertyFromAnywhere(propertyAliases, true);
    }

    protected String getSensitiveProperty(String property) {
        return getSensitiveProperty(new String[]{property});
    }

    protected String getPropertyFromAnywhere(String[] propertyAliases, boolean sensitive) {
        for (String propertyAlias : propertyAliases) {
            final String fromFile = getPropertyFromFile(propertyAlias);
            final String fromCommandLine = getCommandLineArgument(propertyAlias);
            if (fromFile != null && sensitive) LOGGER.warn(String.format("Property %s is included in properties file. Be careful.", propertyAlias));
            if (fromCommandLine != null) return fromCommandLine;
            if (fromFile != null) return fromFile;
        }
        return null;
    }

    protected String getPropertyFromAnywhere(String[] propertyAliases) {
        return getPropertyFromAnywhere(propertyAliases, false);
    }

    protected String getPropertyFromAnywhere(String property) {
        return getPropertyFromAnywhere(new String[]{property}, false);
    }

    protected String getStringProperty(boolean isSensitive, String property, String defaultValue) {
        final String providedValue = getPropertyFromAnywhere(new String[]{property}, isSensitive);

        return (providedValue != null) ? providedValue : defaultValue;
    }

    protected boolean getBooleanProperty(String property, boolean defaultValue) {
        final String providedValue = getPropertyFromAnywhere(property);

        return (providedValue != null) ? Boolean.parseBoolean(providedValue) : defaultValue;
    }

    protected int getIntProperty(String property, int defaultValue) {
        final String providedValue = getPropertyFromAnywhere(property);

        return (providedValue != null) ? Integer.parseInt(providedValue) : defaultValue;
    }

    private String getCommandLineArgument(String property) {
        return System.getProperty(property);
    }

    private String getPropertyFromFile(String property) {
        return getPropertiesFromFile().getProperty(property);
    }

}
