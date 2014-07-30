package org.directcode.ci.core.plugins

import groovy.transform.CompileStatic
import org.directcode.ci.api.Plugin
import org.directcode.ci.core.CI
import org.directcode.ci.plugins.*

/**
 * Plugin Provider for loading bundled plugins
 */
@CompileStatic
class BundledPluginProvider extends PluginProvider {
    static final List<Class<? extends Plugin>> bundledPlugins = [
            GradlePlugin,
            GitPlugin,
            MakePlugin,
            AntPlugin,
            MavenPlugin,
            GroovyScriptPlugin,
            DownloadSourcePlugin,
            MercurialPlugin
    ]

    @Override
    void loadPlugins() {
        bundledPlugins.each { Class<? extends Plugin> plugin ->
            if (!CI.get().config.pluginsSection()["disabled"]?.contains(plugin.name)) {
                plugin.getConstructor().newInstance().apply()
            }
        }
    }
}
