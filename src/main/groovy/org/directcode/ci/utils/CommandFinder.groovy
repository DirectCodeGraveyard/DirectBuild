package org.directcode.ci.utils

import groovy.transform.CompileStatic
import groovy.transform.Memoized
import org.directcode.ci.core.CI
import org.jetbrains.annotations.NotNull

@CompileStatic
class CommandFinder {
    /**
     * Finds a Command using all the methods described in the CommandFinder class.
     * @param command command name
     * @return executable file
     */
    @Memoized(maxCacheSize = 10)
    static File find(@NotNull String command) {
        def path = getPath(command)
        if (path == null) {
            path = findOnPath(command)
        }
        return path
    }

    /**
     * Gets the Path from the CI Paths Configuration
     * @param entryName entry name
     * @return path
     */
    @Memoized(maxCacheSize = 10)
    static File getPath(@NotNull String entryName) {
        def paths = CI.get().config.pathsSection()
        paths.containsKey(entryName) ? new File(paths[entryName]).absoluteFile : null
    }

    /**
     * Finds a Command on path
     * @param command command name
     * @return path
     */
    @Memoized(maxCacheSize = 10)
    static File findOnPath(@NotNull String command) {
        command = actualCommand(command)
        def pathDirs = System.getenv("PATH").tokenize(File.pathSeparator)

        File executable = null
        for (pathDir in pathDirs) {
            def file = new File(pathDir, command)
            if (file.file && file.canExecute()) {
                executable = file
                break
            }
        }
        return executable?.absoluteFile
    }

    /**
     * Finds the Actual Command name, which basically gives a .exe if on Windows
     * @param command command to use
     * @return actual command
     */
    @Memoized(maxCacheSize = 10)
    static String actualCommand(@NotNull String command) {
        OperatingSystem.current().windows ? "${command}.exe" : command
    }

    /**
     * Gives a Script name based on the inputs and the current OS.
     * @param base base directory
     * @param windows windows script name
     * @param unix unix script name
     * @return script file
     */
    @Memoized(maxCacheSize = 10)
    static File forScript(@NotNull File base, @NotNull String windows, @NotNull String unix) {
        OperatingSystem.current().windows ? new File(base, windows).absoluteFile : new File(base, unix).absoluteFile
    }

    /**
     * Gets the Shell Command as configured
     * @return Shell Command
     */
    @Memoized
    static File shell() {
        return OperatingSystem.current().windows ? find("cmd") : find("shell") ?: find("bash") ?: find("sh")
    }
}
