package org.directcode.ci.plugins

import org.directcode.ci.api.Plugin
import org.directcode.ci.tasks.MakeTask

class MakePlugin extends Plugin {

    @Override
    void apply() {
        ci().registerTask("make", MakeTask)
    }
}
