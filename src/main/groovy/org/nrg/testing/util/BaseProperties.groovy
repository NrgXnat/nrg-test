package org.nrg.testing.util

import org.apache.log4j.Logger
import org.nrg.testing.FileIOUtils

class BaseProperties {

    private static final Logger LOGGER = Logger.getLogger(BaseProperties)
    private static final String XNAT_CONFIG_FOLDER = '/config'
    protected final String configProperty
    protected final String defaultConfig
    protected final Properties props

    BaseProperties(String configProperty, String defaultConfig) {
        this.configProperty = configProperty
        this.defaultConfig = defaultConfig
        final String config = System.getProperty(configProperty)
        if (config == null) {
            LOGGER.debug("${configProperty} variable not specified, using default, ${defaultConfig}")
        }
        props = new Properties()
        final String configPath = "${XNAT_CONFIG_FOLDER}/${config ?: defaultConfig}"
        final InputStream configStream = FileIOUtils.loadResourceAsStream("${XNAT_CONFIG_FOLDER}/${config ?: defaultConfig}")
        if (configStream == null){
            throw new RuntimeException("Config file, ${configPath}, not found")
        }
        props.load(configStream)
        LOGGER.debug("Loaded properties from classpath location: ${configPath}")
    }

    protected String getSensitiveProperty(List<String> propertyAliases) {
        getPropertyFromAnywhere(propertyAliases, true)
    }

    protected String getSensitiveProperty(String property) {
        getSensitiveProperty([property])
    }

    protected String getPropertyFromAnywhere(List<String> propertyAliases, boolean sensitive) {
        propertyAliases.findResult { property ->
            final String fromFile = props.getProperty(property)
            if (fromFile != null && sensitive) {
                LOGGER.warn("Property ${property} is included in properties file. Be careful.")
            }
            getCommandLineArgument(property) ?: fromFile
        }
    }

    protected String getPropertyFromAnywhere(List<String> propertyAliases) {
        getPropertyFromAnywhere(propertyAliases, false)
    }

    protected String getPropertyFromAnywhere(String property) {
        getPropertyFromAnywhere([property], false)
    }

    protected String getStringProperty(boolean isSensitive, String property, String defaultValue) {
        getPropertyFromAnywhere([property], isSensitive) ?: defaultValue
    }

    protected boolean getBooleanProperty(String property, boolean defaultValue) {
        final String providedValue = getPropertyFromAnywhere(property)
        (providedValue != null) ? Boolean.parseBoolean(providedValue) : defaultValue
    }

    protected int getIntProperty(String property, int defaultValue) {
        final String providedValue = getPropertyFromAnywhere(property)
        (providedValue != null) ? Integer.parseInt(providedValue) : defaultValue
    }

    private String getCommandLineArgument(String property) {
        System.getProperty(property)
    }

}
