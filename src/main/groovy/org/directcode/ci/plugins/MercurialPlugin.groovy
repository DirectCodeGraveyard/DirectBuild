package org.directcode.ci.plugins

import org.directcode.ci.api.Plugin
import org.directcode.ci.source.MercurialSource
import org.directcode.ci.tasks.MercurialTask

class MercurialPlugin extends Plugin {
    @Override
    void apply() {
        ci().registerTask("mercurial", MercurialTask)
        ci().registerSource("mercurial", MercurialSource)
    }
}
