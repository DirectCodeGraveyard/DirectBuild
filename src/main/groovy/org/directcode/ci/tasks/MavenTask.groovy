package org.directcode.ci.tasks

import groovy.transform.CompileStatic
import org.directcode.ci.api.Task
import org.directcode.ci.exception.ToolMissingException
import org.directcode.ci.utils.CommandFinder

@CompileStatic
class MavenTask extends Task {
    List<String> goals = []
    List<String> opts = []

    @Override
    void execute() {
        def cmd = []

        def maven = CommandFinder.find("mvn")

        if (maven == null) {
            throw new ToolMissingException("Maven is not installed on this system.")
        }

        cmd.with {
            add(maven)
            addAll(opts)
            addAll(goals)
        }

        run(cmd)
    }

    @Override
    void configure(Closure closure) {
        with(closure)
    }
}