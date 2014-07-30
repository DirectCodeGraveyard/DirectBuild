package org.directcode.ci.source

import groovy.transform.CompileStatic
import org.directcode.ci.api.Source
import org.directcode.ci.exception.TaskFailedException
import org.directcode.ci.exception.ToolMissingException
import org.directcode.ci.utils.CommandFinder

@CompileStatic
class GitSource extends Source implements VCS {

    void gitClone() {
        def cmd = []
        cmd << findGit().absolutePath
        cmd << "clone"
        cmd << "--recursive"
        cmd << "--depth"
        cmd << option("depth", 10)
        if (option("branch")) {
            cmd << "--branch"
            cmd << option("branch")
        }
        if (!option("url")) {
            throw new TaskFailedException("Git requires the 'url' option!")
        }
        cmd << option("url")
        cmd << job.buildDir.absolutePath
        run(cmd)
        updateSubmodules()
    }

    void update() {
        def cmd = []
        cmd << findGit().absolutePath
        cmd << "pull"
        cmd << "--all"

        if (option("branch")) {
            run([findGit().absolutePath, "checkout", job.source["branch"] as String])
        }

        updateSubmodules()

        run(cmd)
    }

    boolean exists() {
        return new File(job.buildDir, ".git").exists()
    }

    @Override
    void execute() {
        if (option("clean", true)) {
            job.buildDir.with {
                deleteDir()
                mkdirs()
            }
        }

        if (exists()) {
            update()
        } else {
            gitClone()
        }
    }

    @Override
    VCSChangelog changelog(int count) {
        def changelog = new VCSChangelog()

        if (!exists()) {
            gitClone()
        }

        def proc = execute([findGit().absolutePath, "log", "-${count}".toString(), "--pretty=%H%n%an%n%s"])

        proc.waitFor()

        def log = proc.text.readLines()

        def current = changelog.newEntry()
        def type = 1
        log.each { entry ->
            //noinspection GroovySwitchStatementWithNoDefault
            switch (type) {
                case 1:
                    type++
                    current.revision = entry
                    break
                case 2:
                    type++
                    current.author = entry
                    break
                case 3:
                    type = 1
                    current.message = entry
                    current = changelog.newEntry()
                    break
            }
        }

        changelog.entries.removeAll { entry ->
            !entry["message"] || !entry["revision"] || !entry["author"]
        }

        return changelog
    }

    static File findGit() {
        def gitCommand = CommandFinder.find("git")
        if (gitCommand == null) {
            throw new ToolMissingException("Could not find Git on System!")
        }
        return gitCommand
    }

    void updateSubmodules() {
        def cmd = []
        cmd << findGit().absolutePath
        cmd << "submodule"
        cmd << "update"
        cmd << "--init"
        cmd << "--recursive"
        run(cmd)
    }

    Process execute(List<String> command) {
        def builder = new ProcessBuilder(command)
        builder.directory(job.buildDir)
        builder.redirectErrorStream(true)
        return builder.start()
    }
}
