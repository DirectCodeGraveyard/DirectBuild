package org.directcode.ci.logging

import groovy.transform.CompileStatic
import org.directcode.ci.core.EventBus
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

import java.nio.file.Path
import java.text.DateFormat
import java.text.SimpleDateFormat

@CompileStatic
class Logger {
    /**
     * Logger Pool
     */
    private static final Map<String, Logger> loggers = [:]

    /**
     * Logger Date Format
     */
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.default)

    /**
     * Logger Name
     */
    final String name

    /**
     * Logger Event Bus
     */
    private final EventBus eventBus = new EventBus()

    /**
     * Current Log Level
     */
    LogLevel currentLevel = LogLevel.INFO

    /**
     * Creates a new Logger
     * @param name logger name
     */
    private Logger(@NotNull String name) {
        this.name = name
    }

    /**
     * Sets the Log Level for all loggers in the Log Pool
     * @param level Log Level
     */
    static void setGlobalLogLevel(@NotNull LogLevel level) {
        loggers.values().each { logger ->
            logger.currentLevel = level
        }
    }

    /**
     * Logs all loggers to the specified file.
     * @param path file path
     */
    static void logAllTo(@NotNull Path path) {
        loggers.values().each { logger ->
            logger.logTo(path)
        }
    }

    /**
     * Gets a Logger from the Logger Pool
     * @param name logger name
     * @return logger
     */
    static Logger forName(@NotNull String name) {
        if (name in loggers) {
            return loggers[name]
        } else {
            return (loggers[name] = new Logger(name))
        }
    }

    /**
     * Checks if the Log Level can be logged
     * @param input Log level
     * @return true if the level can be logged, otherwise false
     */
    boolean canLog(@NotNull LogLevel input) {
        currentLevel.ordinal() >= input.ordinal()
    }

    /**
     * Logs a message with the specified log level. Optional Exception may be specified.
     * @param level log level
     * @param message message to log
     * @param e exception
     */
    void log(@NotNull LogLevel level, @NotNull String message, @Nullable Throwable e = null) {
        if (canLog(level)) {
            def timestamp = dateFormat.format(new Date())
            def complete = "[${timestamp}][${name}][${level.name()}] ${message}"
            def cancelled = false
            eventBus.dispatch("log", [
                    level    : level,
                    message  : message,
                    exception: e,
                    timestamp: timestamp,
                    complete : complete,
                    cancel   : { boolean cancel = true ->
                        cancelled = cancel
                    }
            ])
            if (!cancelled) {
                println complete
                if (e) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * Logs a message with the INFO log level
     * @param message log message
     */
    void info(@NotNull String message) {
        log(LogLevel.INFO, message)
    }

    /**
     * Logs a message with the WARNING log level
     * @param message log message
     */
    void warning(@NotNull String message) {
        log(LogLevel.WARNING, message)
    }

    /**
     * Logs a message with the DEBUG log level
     * @param message log message
     */
    void debug(@NotNull String message) {
        log(LogLevel.DEBUG, message)
    }

    /**
     * Logs a message with the ERROR log level
     * @param message log message
     * @param e exception
     */
    void error(@NotNull String message, @Nullable Throwable e = null) {
        log(LogLevel.ERROR, message, e)
    }

    /**
     * Logs the Loggers output to a file
     * @param path file path
     */
    void logTo(@NotNull Path path) {
        eventBus.on('log') { event ->
            def message = event["complete"] as String
            def exception = event["exception"] as Exception
            path.append("${message}\n")
            if (exception) {
                exception.printStackTrace(path.newPrintWriter())
            }
        }
    }

    /**
     * Adds an Event Handler for the Logger Event Bus
     * @param eventName event name
     * @param handler handler to register
     */
    void on(@NotNull String eventName, @NotNull Closure handler) {
        eventBus.on(eventName, handler)
    }
}