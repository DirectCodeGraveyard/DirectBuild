package org.directcode.ci.tasks

import groovy.transform.CompileStatic
import org.directcode.ci.api.Task
import org.directcode.ci.exception.ToolMissingException
import org.directcode.ci.utils.CommandFinder

@CompileStatic
class MakeTask extends Task {

    List<String> targets = []

    @Override
    void execute() {
        def command = []
        def make = CommandFinder.find("make")
        if (make == null) {
            throw new ToolMissingException("Make not found on this system.")
        }
        command.with {
            add(make)
            addAll(targets)
        }
        run(command)
    }

    @Override
    void configure(Closure closure) {
        with(closure)
    }
}
