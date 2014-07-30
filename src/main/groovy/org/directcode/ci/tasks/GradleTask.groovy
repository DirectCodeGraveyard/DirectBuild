package org.directcode.ci.tasks

import groovy.transform.CompileStatic
import org.directcode.ci.api.Task
import org.directcode.ci.exception.ToolMissingException
import org.directcode.ci.utils.CommandFinder
import org.directcode.ci.utils.OperatingSystem

@CompileStatic
class GradleTask extends Task {
    boolean wrapper = false
    List<String> opts = []
    List<String> tasks = []

    @Override
    void execute() {
        def command = []

        if (wrapper) {
            if (!file(job.buildDir, "gradlew").exists()) {
                throw new ToolMissingException("Gradle Wrapper not found in Job: ${job.name}")
            }

            if (OperatingSystem.current().unix) {
                command.add(CommandFinder.shell())
            }

            command.add(CommandFinder.forScript(job.buildDir, "gradlew.bat", "gradlew"))
        } else {
            def c = CommandFinder.find("gradle")
            if (c == null) {
                throw new ToolMissingException("Gradle not found on this system.")
            }
            command.add(c.absolutePath)
        }

        command.with {
            addAll(opts)
            addAll(tasks)
        }

        run(command)
    }

    @Override
    void configure(@DelegatesTo(GradleTask) Closure closure) {
        with(closure)
    }
}
