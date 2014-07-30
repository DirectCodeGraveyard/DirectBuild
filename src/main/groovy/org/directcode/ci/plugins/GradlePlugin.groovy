package org.directcode.ci.plugins

import org.directcode.ci.api.Plugin
import org.directcode.ci.tasks.GradleTask

class GradlePlugin extends Plugin {
    @Override
    void apply() {
        ci().registerTask("gradle", GradleTask)
    }
}
