package org.directcode.ci.core.plugins

import groovy.transform.CompileStatic

/**
 * A Plugin Provider will allow extending the plugin system to include multiple types of org.directcode.ci.plugins.
 */
@CompileStatic
abstract class PluginProvider {
    abstract void loadPlugins();
}
