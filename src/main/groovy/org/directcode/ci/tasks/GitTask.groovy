package org.directcode.ci.tasks

import groovy.transform.CompileStatic
import org.directcode.ci.api.Task
import org.directcode.ci.exception.ToolMissingException
import org.directcode.ci.utils.CommandFinder

@CompileStatic
class GitTask extends Task {
    List<String> args = []

    @Override
    void execute() {
        def cmd = []
        def git = CommandFinder.find("git")
        if (git == null) {
            throw new ToolMissingException("Git was not found on this system!")
        }
        cmd.with {
            add git
            addAll(args)
        }
    }

    @Override
    void configure(Closure closure) {
        with(closure)
    }
}
