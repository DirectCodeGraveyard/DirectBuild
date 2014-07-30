package org.directcode.ci.tasks

import groovy.transform.CompileStatic
import org.directcode.ci.api.Task
import org.directcode.ci.exception.TaskFailedException
import org.directcode.ci.exception.ToolMissingException
import org.directcode.ci.utils.CommandFinder

@CompileStatic
class GroovyScriptTask extends Task {
    List<String> opts = []
    String script

    @Override
    void execute() {
        if (!script) {
            throw new TaskFailedException("Script must be specified.")
        }
        def cmd = []
        def file = new File(job.buildDir, script).absoluteFile
        if (!file.exists()) {
            throw new TaskFailedException("Script file '${script}' does not exist!")
        }
        def groovy = CommandFinder.find("groovy")
        if (groovy == null) {
            throw new ToolMissingException("Groovy not found on this system.")
        }
        cmd << groovy
        cmd.addAll(opts)
        cmd << file
    }

    @Override
    void configure(Closure closure) {
        basicConfigure(this, closure)
    }
}
