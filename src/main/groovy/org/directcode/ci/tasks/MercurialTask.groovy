package org.directcode.ci.tasks

import groovy.transform.CompileStatic
import org.directcode.ci.api.Task
import org.directcode.ci.exception.ToolMissingException
import org.directcode.ci.utils.CommandFinder

@CompileStatic
class MercurialTask extends Task {
    List<String> args = []

    @Override
    void execute() {
        def cmd = []
        def mercurial = CommandFinder.find("hg")
        if (mercurial == null) {
            throw new ToolMissingException("Mercurial was not found on this system!")
        }
        cmd.with {
            add mercurial
            addAll(args)
        }
    }

    @Override
    void configure(Closure closure) {
        with(closure)
    }

}
