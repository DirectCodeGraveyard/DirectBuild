package org.directcode.ci.plugins

import org.directcode.ci.api.Plugin
import org.directcode.ci.tasks.AntTask

class AntPlugin extends Plugin {
    @Override
    void apply() {
        ci().registerTask("ant", AntTask)
    }
}
