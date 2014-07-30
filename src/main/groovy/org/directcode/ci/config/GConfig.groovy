package org.directcode.ci.config

import groovy.transform.CompileStatic
import org.directcode.ci.utils.Utils

@CompileStatic
class GConfig {
    private File configFile
    private Binding config
    private String defaultConfig

    GConfig(File configFile = null) {
        this.configFile = configFile
    }

    void setDefaultConfig(String defaultConfig) {
        this.defaultConfig = defaultConfig
    }

    void setConfigFile(File file) {
        this.configFile = file
    }

    void load() {
        if (!configFile.exists()) {
            configFile.write(defaultConfig)
        }
        def configScript = Utils.parseConfig(configFile)

        configScript.run()

        this.config = configScript.binding
    }

    Object getProperty(String key) {
        metaClass.hasProperty(key) ? metaClass.getProperty(this, key) : config.getVariable(key)
    }

    Object getProperty(String key, Object defaultValue) {
        !hasProperty(key) ? defaultValue : getProperty(key)
    }

    void setProperty(String key, Object value) {
        if (metaClass.hasProperty(this, key)) {
            metaClass.setProperty(this, key, value)
        } else {
            this.config.setVariable(key, value)
        }
    }

    boolean hasProperty(String key) {
        metaClass.hasProperty(this, key) || config.hasVariable(key)
    }
}