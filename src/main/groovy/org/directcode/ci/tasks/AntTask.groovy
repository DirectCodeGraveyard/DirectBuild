package org.directcode.ci.tasks

import groovy.transform.CompileStatic
import org.directcode.ci.api.Task
import org.directcode.ci.exception.ToolMissingException
import org.directcode.ci.utils.CommandFinder

@CompileStatic
class AntTask extends Task {
    List<String> targets = []
    List<String> opts = []

    @Override
    void execute() {
        def cmd = []

        def ant = CommandFinder.find("ant")

        if (ant == null) {
            throw new ToolMissingException("Ant was not found on this system.")
        }

        cmd.with {
            add ant
            addAll(opts)
            addAll(targets)
        }

        run(cmd)
    }

    @Override
    void configure(Closure closure) {
        with(closure)
    }
}