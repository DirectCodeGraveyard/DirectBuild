package org.directcode.ci.logging

import groovy.transform.CompileStatic
import org.jetbrains.annotations.NotNull

/**
 * Represents a Log Level.
 */
@CompileStatic
enum LogLevel {
    /* Arranged from least output to most output */
    DISABLED, ERROR, WARNING, INFO, DEBUG;

    /**
     * Parses a log level from a string. If not found, returns disabled.
     * @param name log level name
     * @return Log Level
     */
    static LogLevel parse(@NotNull String name) {
        if (!(values()*.name().contains(name))) {
            return DISABLED
        } else {
            return values().find {
                it.name() == name
            }
        }
    }
}