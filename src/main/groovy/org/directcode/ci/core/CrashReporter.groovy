package org.directcode.ci.core

import groovy.transform.CompileStatic
import org.directcode.ci.logging.LogLevel
import org.directcode.ci.logging.Logger
import org.directcode.ci.utils.HTTP
import org.directcode.ci.utils.Utils
import org.jetbrains.annotations.NotNull

import java.nio.file.Path

/**
 * Reports Exceptions from SimpleCI to the crash reporter
 */
@CompileStatic
class CrashReporter {
    static final Logger logger = Logger.forName("CrashReporter")

    static
    final String reporter = "https://script.google.com/macros/s/AKfycby4kKJjBVLyrfS83qec8_nJBzSWN2LKqfNMDzBsph_R20tfOhc/exec"

    static boolean reporting = false

    static void report(@NotNull Path path) {
        if (logger.canLog(LogLevel.DEBUG)) {
            return
        }
        if (reporting) {
            while (reporting) {
                sleep(20)
            }
        }
        reporting = true
        logger.error("An unexpected error occurred in SimpleCI")

        if (!Main.noReports() && Utils.online) {
            HTTP.post(data: [log: path.text], url: reporter) { Map<String, Object> data ->
                logger.error("Sending Crash Report")
                def id = data.get("data", "Unknown") as String
                logger.error("Crash Report Sent. ID: ${id}")
            }
        }
        Runtime.runtime.halt(1)
        reporting = false
    }
}