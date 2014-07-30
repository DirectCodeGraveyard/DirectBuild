package org.directcode.ci.build

import groovy.transform.CompileStatic
import org.directcode.ci.api.Source
import org.directcode.ci.api.Task
import org.directcode.ci.core.CI
import org.directcode.ci.exception.CIException
import org.directcode.ci.jobs.Job
import org.directcode.ci.logging.Logger
import org.directcode.ci.utils.ExecutionTimer
import org.directcode.ci.utils.Utils

/**
 * Represents a Build of a Job
 */
@CompileStatic
class Build implements Comparable<Build> {

    static final Logger logger = Logger.forName("Builder")

    /**
     * The Build's Job
     */
    final Job job

    /**
     * The Build's Number
     */
    final int number

    private boolean complete
    private boolean waiting

    /**
     * Creates a new build
     * @param job Job to build
     * @param number Build Number
     * NOTICE: Internal Use Only
     */
    Build(Job job, int number) {
        this.job = job
        this.number = number
        this.complete = false
        this.waiting = true
    }

    /**
     * Executes this build
     * NOTICE: Internal Only
     */
    void execute() {
        def ci = CI.get()
        this.waiting = false

        ci.dispatch("ci.build.started", [
                jobName: job.name,
                number : number,
                build  : this
        ])

        def timer = new ExecutionTimer()

        timer.start()

        def success = true
        def scmShouldRun = true
        def tasksShouldRun = true

        job.status = BuildStatus.RUNNING
        logger.info "Build '${job.name}:${number}' is building"

        if (!job.buildDir.exists()) {
            job.buildDir.mkdirs()
        }

        def buildLog = new BuildLog(job.logFile, this)

        if (scmShouldRun) {
            def scmConfig = job.source

            if (ci.sourceTypes.containsKey(scmConfig.type)) {
                Source source = ((Class<? extends Source>) ci.sourceTypes[scmConfig.type as String]).getConstructor().newInstance()

                source.job = job
                source.log = buildLog

                try {
                    source.execute()
                } catch (CIException e) {
                    logger.info "Job '${job.name}' (Source): ${e.message}"
                    tasksShouldRun = false
                    success = false
                }
            } else {
                logger.error "Build '${job.name}:${number}' is attempting to use a non-existant Source Type '${scmConfig.type}!'"
                success = false
                tasksShouldRun = false
            }
        }

        if (tasksShouldRun) {
            def tasks = job.tasks

            for (taskConfig in tasks) {
                def id = tasks.indexOf(taskConfig) + 1
                logger.info "Running Task ${id} of ${job.tasks.size()} for Build '${job.name}:${number}'"

                if (!(taskConfig.type in ci.taskTypes.keySet())) {
                    logger.error("Build '${job.name}:${number}': Unknown task type '${taskConfig.type}'")
                    success = false
                    break
                }

                Task task = taskConfig.create()

                task.job = job
                task.build = this
                task.log = buildLog

                task.configure(taskConfig.config)

                try {
                    task.execute()
                } catch (e) {
                    logger.error("Build '${job.name}:${number}' (Task #${id}): ${e.message}")
                    success = false
                    break
                }
            }

            def artifactsDir = ci.file("artifacts/${job.name}/${number}")
            artifactsDir.mkdirs()
            job.artifacts.files.each { String location ->
                def source = new File(job.buildDir, location)
                def target = new File(artifactsDir, source.name)
                if (!source.exists()) {
                    buildLog.write("Artifact '${location}' does not exist. Skipping.")
                    return
                }
                target.bytes = source.bytes
            }
        }

        def buildTime = timer.stop()

        logger.debug "Build '${job.name}:${number}' completed in ${buildTime} milliseconds"

        if (success) {
            logger.info "Build '${job.name}:${number}' has Completed"
            job.status = BuildStatus.SUCCESS
        } else {
            logger.info "Build '${job.name}:${number}' has Failed"
            job.status = BuildStatus.FAILURE
        }

        ci.dispatch("ci.build.done", [
                jobName   : job.name,
                status    : job.status,
                buildTime : buildTime,
                timeString: timer.toString(),
                number    : number,
                build     : this
        ])

        buildLog.complete()

        def log = job.logFile.text

        def base64Log = Utils.encodeBase64(log)

        def job_history = ci.storage.get("job_history")

        def historyListOriginal = (List<Map<String, ? extends Object>>) job_history.get(job.name, new LinkedList<>())

        def history = (List<Map<String, ? extends Object>>) []

        historyListOriginal.each { it ->
            history.add(it)
        }

        history.add([number: number, status: job.status.ordinal(), log: base64Log, buildTime: buildTime, timeStamp: new Date().toString()])

        job_history[job.name] = history

        this.complete = true
    }

    /**
     * Checks if the build has completed
     * @return true if the build has completed, else false.
     */
    boolean isComplete() {
        return complete
    }

    /**
     * Checks if the build is waiting in the Queue
     * @return true if the build is waiting, else false.
     */
    boolean isWaiting() {
        return waiting
    }

    /**
     * Checks if the build is running
     * @return true if the build is running, otherwise false.
     */
    boolean isRunning() {
        return !waiting && !complete
    }

    /**
     * Waits until the build is complete.
     */
    void waitFor() {
        while (waiting || running) {
            sleep(5)
        }
    }

    @Override
    int compareTo(Build other) {
        return number.compareTo(other.number)
    }
}
