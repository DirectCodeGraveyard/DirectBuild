package org.directcode.ci.tasks

import groovy.transform.CompileStatic
import org.directcode.ci.api.Task

/**
 * Executes a Command
 */
@CompileStatic
class CommandTask extends Task {

    String command

    @Override
    void execute() {
        run(command.tokenize())
    }

    @Override
    void configure(Closure closure) {
        with(closure)
    }
}
