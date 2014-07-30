package org.directcode.ci.plugins

import org.directcode.ci.api.Plugin
import org.directcode.ci.source.GitSource
import org.directcode.ci.tasks.GitTask

class GitPlugin extends Plugin {
    @Override
    void apply() {
        ci().registerTask("git", GitTask)
        ci().registerSource("git", GitSource)
    }
}
