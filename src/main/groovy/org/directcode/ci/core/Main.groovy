package org.directcode.ci.core

import groovy.transform.CompileStatic
import gutter.console.ConsoleHandler
import org.apache.log4j.Level as Log4jLevel
import org.apache.log4j.Logger as Log4j
import org.directcode.ci.jobs.Job
import org.directcode.ci.logging.LogLevel
import org.directcode.ci.logging.Logger
import org.directcode.ci.utils.OperatingSystem
import org.directcode.ci.utils.ReleaseInfo
import org.directcode.ci.utils.SystemProperties
import org.directcode.ci.utils.Utils
import org.jetbrains.annotations.NotNull

/**
 * Main Entry Class to DirectBuild
 */
@CompileStatic
class Main {

    static final Logger logger = Logger.forName("Console")
    static boolean noReports

    /**
     * Main Entry Method to DirectBuild
     * @param consoleArgs arguments supplied on console
     */
    static void main(@NotNull String[] consoleArgs) {

        if ("--info" in consoleArgs) {
            println "Revision: ${ReleaseInfo.gitCommitSHA()}"
            println "Build Date: ${ReleaseInfo.buildDate()}"
            return
        }

        if (OperatingSystem.current().unsupported) {
            logger.warning("DirectBuild does not officially support your platform.")
        }

        noReports = "--no-reports" in consoleArgs

        /* Configure log4j to fix warnings */
        Log4j.rootLogger.level = Log4jLevel.OFF

        Thread.defaultUncaughtExceptionHandler = [
                uncaughtException: { Thread thread, Throwable e ->
                    if (logger.canLog(LogLevel.DEBUG) || SystemProperties.getBoolean("ci.debug")) {
                        e.printStackTrace()
                        System.exit(1)
                        return
                    }
                    def output = new File("ci.log").toPath()
                    output.append("${Utils.exceptionToString(e)}")
                    CrashReporter.report(output)
                }
        ] as Thread.UncaughtExceptionHandler

        def ci = CI.get()

        ci.start()

        ConsoleHandler.loop { String command, List<String> args ->
            if (command == 'build') {

                if (args.size() == 0) {
                    println "Usage: build <job>"
                    return
                }

                def jobName = args[0]

                def job = ci.jobs[jobName] as Job

                if (job == null) {
                    println "No Such Job: ${jobName}"
                } else {
                    ci.build(job)
                }
            } else if (command == 'stop') {
                ci.stop()
                System.exit(0)
            } else if (command == 'clean') {
                if (args.size() == 0) {
                    println "Usage: clean <job>"
                    return
                }

                def jobName = args[0]

                Job job = ci.jobs[jobName]

                if (job == null) {
                    println "No Such Job: ${jobName}"
                } else {
                    ci.logger.info "Cleaning Workspace for Job '${jobName}'"
                    job.buildDir.deleteDir()
                }
            } else if (command == "reload") {
                if (args.size() == 0) {
                    ci.updateJobs()
                    logger.info("All Jobs were reloaded")
                } else if (args.size() == 1) {
                    def jobName = args[0]
                    if (jobName in ci.jobs.keySet()) {
                        ci.jobs[jobName].reload()
                        logger.info("Job '${jobName}' was reloaded")
                    } else {
                        println "No Such Job: ${args[0]}"
                    }
                } else {
                    println "Usage: reload [job]"
                }
            }
        }
    }

    /**
     * Checks if the Console has been called with --no-reports
     * @return true if specified, else false
     */
    static boolean noReports() {
        noReports
    }
}
