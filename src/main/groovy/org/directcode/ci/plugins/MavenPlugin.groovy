package org.directcode.ci.plugins

import org.directcode.ci.api.Plugin
import org.directcode.ci.tasks.MavenTask

class MavenPlugin extends Plugin {
    @Override
    void apply() {
        ci().registerTask("maven", MavenTask)
    }
}
