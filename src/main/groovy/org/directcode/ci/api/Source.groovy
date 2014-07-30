package org.directcode.ci.api

import org.directcode.ci.build.BuildLog
import org.directcode.ci.core.CI
import org.directcode.ci.exception.TaskFailedException
import org.directcode.ci.jobs.Job
import org.directcode.ci.utils.Utils
import org.jetbrains.annotations.NotNull

/**
 * A Source is a provider for resources in the Build Workspace
 */
abstract class Source {
    Job job
    BuildLog log

    abstract void execute();

    int run(
            @NotNull List<String> command,
            @NotNull File workingDir = job.buildDir,
            @NotNull Map<String, String> env = [TERM: "dumb"], @NotNull boolean handleExitCode = true) {
        CI.logger.debug("Executing: '${command.join(" ")}'")
        log.write("\$ '${command.join(' ')}'")

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
        return result?.code ?: -1
    }

    Object option(String key, Object defaultValue = null) {
        return job.source.get(key, defaultValue)
    }
}
