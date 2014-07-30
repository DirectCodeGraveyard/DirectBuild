package org.directcode.ci.utils

import groovy.transform.CompileStatic
import groovy.transform.Memoized

@CompileStatic
class OperatingSystem {
    private final String name

    OperatingSystem() {
        this(System.getProperty("os.name"))
    }

    OperatingSystem(String name) {
        this.name = name.toLowerCase()
    }

    @Memoized
    static OperatingSystem current() {
        return new OperatingSystem()
    }

    @Memoized(maxCacheSize = 10)
    static OperatingSystem forName(String name) {
        return new OperatingSystem(name)
    }

    boolean isWindows() {
        return name.contains("windows")
    }

    boolean isUnix() {
        return name.contains("nix") || name.contains("nux") || name.contains("aix")
    }

    boolean isUnsupported() {
        return !(unix || windows || OSX)
    }

    boolean isOSX() {
        return name.contains("mac")
    }

    String getScriptExtension() {
        return windows ? ".bat" : ".sh"
    }

    String getScriptFirstLine() {
        return windows ? "@echo off" : "#!${CommandFinder.shell().absolutePath}"
    }
}
