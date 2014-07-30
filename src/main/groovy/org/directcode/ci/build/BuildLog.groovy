package org.directcode.ci.build

import groovy.transform.CompileStatic
import org.directcode.ci.core.CI
import org.jetbrains.annotations.NotNull

/**
 * Interface to the Job Log File
 */
@CompileStatic
class BuildLog {
    /**
     * Log File
     */
    final File file

    /**
     * Output PrintWriter
     */
    final PrintWriter out

    final Build build

    /**
     * Creates a new Job Log
     * @param file Job Log File
     */
    BuildLog(@NotNull File file, Build build) {
        this.file = file
        this.out = file.toPath().newPrintWriter()
        this.build = build
    }

    /**
     * Writes a line to the Job Log
     * @param line line to write
     */
    void write(@NotNull String line) {
        def lineChange = { String input ->
            line = input
        }
        CI.get().dispatch("ci.build.log.write", [line: line, change: lineChange, build: build, log: this])
        out.println(line)
        out.flush()
    }

    /**
     * Called when the Build is complete.
     * Closes the streams.
     */
    void complete() {
        CI.get().dispatch("ci.build.log.complete", [build: build, log: this])
        out.flush()
        out.close()
    }
}
