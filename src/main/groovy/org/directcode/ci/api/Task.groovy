package org.directcode.ci.api

import org.directcode.ci.build.Build
import org.directcode.ci.build.BuildLog
import org.directcode.ci.core.CI
import org.directcode.ci.exception.TaskFailedException
import org.directcode.ci.jobs.Job
import org.directcode.ci.utils.Utils
import org.jetbrains.annotations.NotNull

/**
 * A CI build Task
 */
abstract class Task {
    Job job
    Build build
    BuildLog log

    /**
     * Executes this Task
     */
    abstract void execute();

    /**
     * Configures this Task
     * @param closure closure that will configure the task
     */
    abstract void configure(Closure closure);

    static File file(@NotNull File parent = new File("."), @NotNull String name) {
        return new File(parent, name)
    }

    int run(
            @NotNull List<String> command,
            @NotNull File workingDir = job.buildDir,
            @NotNull Map<String, String> env = [TERM: "dumb"], @NotNull boolean handleExitCode = true) {
        CI.logger.debug("Executing: '${command.join(" ")}'")
        log.write("\$ '${command.join(' ')}'")

        env["JOB_NAME"] = job.name
        env["JOB_BUILD_NUMBER"] = build.number

        def result = Utils.execute { ->
            executable(command[0])
            arguments(command.drop(1))
            directory(workingDir)
            environment(env)
            streamOutput { String line ->
                CI.logger.debug("${line}")
                log.write("${line}")
            }
        }

        log.write(">> Command Complete { code: ${result.code} }")

        if (handleExitCode && result.code != 0) {
            throw new TaskFailedException("Command exited with non-zero status!")
        }

        return result.code
    }

    static void basicConfigure(@NotNull Task task, @NotNull Closure closure) {
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = task
        closure.call()
    }
}
