package org.directcode.ci.core.plugins

import groovy.transform.CompileStatic
import org.directcode.ci.core.CI

@CompileStatic
class PluginManager {
    final List<PluginProvider> providers = []

    PluginManager() {
        providers.add(new JarPluginProvider())
        providers.add(new ScriptPluginProvider())
        providers.add(new BundledPluginProvider())
    }

    void loadPlugins() {
        CI.get().file("plugins").mkdirs()
        providers.each { provider ->
            provider.loadPlugins()
        }
    }
}
