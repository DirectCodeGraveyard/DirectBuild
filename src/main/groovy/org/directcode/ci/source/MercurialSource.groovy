package org.directcode.ci.source

import groovy.transform.CompileStatic
import org.directcode.ci.api.Source
import org.directcode.ci.exception.TaskFailedException
import org.directcode.ci.exception.ToolMissingException
import org.directcode.ci.utils.CommandFinder

@CompileStatic
class MercurialSource extends Source implements VCS {

    void mercurialClone() {
        def cmd = []
        cmd << findMercurial().absolutePath
        cmd << "clone"
        if (!option("url")) {
            throw new TaskFailedException("Mercurial requires the 'url' option!")
        }
        cmd << option("url")
        cmd << job.buildDir.absolutePath
        run(cmd)
    }

    void update() {
        def cmd = []
        cmd << findMercurial().absolutePath
        cmd << "pull"

        run(cmd)
    }

    boolean exists() {
        return new File(job.buildDir, ".hg").exists()
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
            mercurialClone()
        }
    }

    @Override
    VCSChangelog changelog(int count) {
        def changelog = new VCSChangelog()

        if (!exists()) {
            mercurialClone()
        }

        def proc = execute([findMercurial().absolutePath, "log", "-l ${count}".toString(), "\"{node}\\n{author}\\n{desc}\\n\""])

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

    static File findMercurial() {
        def mercurialCommand = CommandFinder.find("hg")
        if (mercurialCommand == null) {
            throw new ToolMissingException("Could not find Mercurial on System!")
        }
        return mercurialCommand
    }

    Process execute(List<String> command) {
        def builder = new ProcessBuilder(command)
        builder.directory(job.buildDir)
        builder.redirectErrorStream(true)
        return builder.start()
    }
}
