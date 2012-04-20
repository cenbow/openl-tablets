package org.openl.rules.webstudio;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Configuration manager.
 * 
 * @author Andrei Astrouski
 * 
 * TODO Move to Commons project
 * TODO Separate configuration sets from the manager
 */
public class ConfigManager {

    private final Log log = LogFactory.getLog(ConfigManager.class);

    private boolean useSystemProperties;
    private String propsLocation;
    private String defaultPropsLocation;
    private boolean autoSave;

    private Configuration systemConfiguration;
    private FileConfiguration configurationToSave;
    private FileConfiguration defaultConfiguration;
    private CompositeConfiguration compositeConfiguration;

    public ConfigManager(boolean useSystemProperties,
            String propsLocation, String defaultPropsLocation) {
        this(useSystemProperties, propsLocation, defaultPropsLocation, false);
    }

    public ConfigManager(boolean useSystemProperties,
            String propsLocation, String defaultPropsLocation, boolean autoSave) {
        this.useSystemProperties = useSystemProperties;
        this.propsLocation = propsLocation;
        this.defaultPropsLocation = defaultPropsLocation;
        this.autoSave = autoSave;

        init();
    }

    private void init() {
        compositeConfiguration = new CompositeConfiguration();

        if (useSystemProperties) {
            systemConfiguration = new SystemConfiguration();
            compositeConfiguration.addConfiguration(systemConfiguration);
        }

        configurationToSave = createFileConfiguration(propsLocation, true);
        if (configurationToSave != null) {
            compositeConfiguration.addConfiguration(configurationToSave);
            if (autoSave) {
                configurationToSave.setAutoSave(autoSave);
            }
        }

        defaultConfiguration = createFileConfiguration(defaultPropsLocation);
        if (defaultConfiguration != null) {
            compositeConfiguration.addConfiguration(defaultConfiguration);
        }
    }

    private FileConfiguration createFileConfiguration(String configLocation, boolean createIfNotExist) {
        PropertiesConfiguration configuration = null;
        if (configLocation != null) {
            try {
                if (createIfNotExist) {
                    configuration = new PropertiesConfiguration(new File(configLocation));
                } else {
                    configuration = new PropertiesConfiguration(configLocation);
                }
            } catch (Exception e) {
                log.error("Error when initializing configuration: " + configLocation, e);
            }
        }
        return configuration;
    }

    private FileConfiguration createFileConfiguration(String configLocation) {
        return createFileConfiguration(configLocation, false);
    }

    public String getStringProperty(String key) {
        return compositeConfiguration.getString(key);
    }

    public boolean getBooleanProperty(String key) {
        return compositeConfiguration.getBoolean(key);
    }

    public Map<String, Object> getProperties() {
        Map<String, Object> properties = new HashMap<String, Object>();
        for (Iterator<?> iterator = compositeConfiguration.getKeys(); iterator.hasNext();) {
            String key = (String) iterator.next();
            properties.put(key, getStringProperty(key));
        }
        return properties;
    }

    public void setProperty(String key, Object value) {
        if (key != null && value != null) {
            String defaultValue = compositeConfiguration.getString(key);
            if (defaultValue != null) {
                if (!defaultValue.equals(value.toString())) {
                    getConfigurationToSave().setProperty(key, value.toString());
                }
            }
        }
    }

    public void removeProperty(String key) {
        getConfigurationToSave().clearProperty(key);
    }

    private FileConfiguration getConfigurationToSave() {
        if (configurationToSave == null) {
            configurationToSave = createFileConfiguration(propsLocation, true);
        }
        return configurationToSave;
    }

    public boolean isSystemProperty(String name) {
        if (systemConfiguration != null && systemConfiguration.getString(name) != null) {
            return true;
        }
        return false;
    }

    public boolean save() {
        if (configurationToSave != null) {
            try {
                configurationToSave.save();
                return true;
            } catch (Exception e) {
                log.error("Error when saving configuration: " + configurationToSave.getBasePath(), e);
            }
        }
        return false;
    }

    public boolean restoreDefaults() {
        if (configurationToSave != null && !configurationToSave.isEmpty()) {
            configurationToSave.clear();
            return save();
        }

        return false;
    }

}
