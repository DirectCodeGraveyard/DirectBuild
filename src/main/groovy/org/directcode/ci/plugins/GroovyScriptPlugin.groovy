package org.directcode.ci.plugins

import org.directcode.ci.api.Plugin
import org.directcode.ci.tasks.GroovyScriptTask

class GroovyScriptPlugin extends Plugin {
    @Override
    void apply() {
        ci().registerTask("groovy", GroovyScriptTask)
    }
}
