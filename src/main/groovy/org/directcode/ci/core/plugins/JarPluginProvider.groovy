package org.directcode.ci.core.plugins

import groovy.transform.CompileStatic
import org.directcode.ci.api.Plugin
import org.directcode.ci.core.CI
import org.directcode.ci.exception.CIException
import org.directcode.ci.utils.FileMatcher

import java.util.jar.JarFile

/**
 * A Plugin Provider that loads plugins from jars.
 * Uses the 'Plugin' manifest entry to find the Plugin class.
 */
@CompileStatic
class JarPluginProvider extends PluginProvider {
    @Override
    void loadPlugins() {
        def pluginsDir = new File(CI.get().configRoot, "plugins")
        FileMatcher.create(pluginsDir).withExtension("jar") { File file ->
            (this.class.classLoader as GroovyClassLoader).addURL(file.toURI().toURL()) // This method will exist at runtime
            def jar = new JarFile(file)
            def manifest = jar.manifest.mainAttributes
            if ("Plugin-Class" in manifest.keySet()) { // A Class that extends Plugin
                def className = manifest.getValue("Plugin-Class")
                if (CI.get().config.pluginsSection()["disabled"]?.contains(className)) {
                    return
                }
                def clazz = this.class.classLoader.loadClass(className)
                if (!clazz.isAssignableFrom(Plugin)) {
                    throw new CIException("Plugin Jar's Class is not an instance of ${Plugin.class.name}")
                }
                def instance = clazz.newInstance() as Plugin
                instance.apply()
            }
        }
    }
}
