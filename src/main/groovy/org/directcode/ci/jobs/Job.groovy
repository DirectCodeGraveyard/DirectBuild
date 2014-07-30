package org.directcode.ci.jobs

import groovy.transform.CompileStatic
import groovy.transform.Memoized
import org.directcode.ci.build.BuildStatus
import org.directcode.ci.config.ArtifactSpec
import org.directcode.ci.config.TaskConfiguration
import org.directcode.ci.core.CI
import org.jetbrains.annotations.NotNull

/**
 * A CI Job
 */
@CompileStatic
class Job {
    private final File jobFile

    private BuildStatus status

    /**
     * Build Configuration Script
     */
    private JobScript buildConfig

    /**
     * Web Hooks Configuration
     */
    final WebHooks webHooks

    /**
     * Creates a new Job from a Job File
     * @param file Job File
     */
    Job(@NotNull File file) {
        this.webHooks = new WebHooks(this)
        this.jobFile = file
        this.buildConfig = JobScript.from(file, this)
        buildDir.mkdirs()
    }

    /**
     * Gets the name of the Job
     * @return name of job
     */
    String getName() {
        buildConfig.name
    }

    /**
     * Gets the Task Configurations of the Job
     * @return task configurations
     */
    List<TaskConfiguration> getTasks() {
        buildConfig.tasks
    }

    /**
     * Gets the Workspace Directory
     * @return workspace directory
     */
    @Memoized
    File getBuildDir() {
        CI.get().file("workspace/${name}").absoluteFile
    }

    /**
     * Gets the Source Configuration
     * @return source configuration
     */
    Map<String, ? extends Object> getSource() {
        buildConfig.source
    }

    /**
     * Returns the Artifacts Specification of the Job
     * @return artifacts specification
     */
    ArtifactSpec getArtifacts() {
        buildConfig.artifacts
    }

    /**
     * Returns the Job Log File
     * @return log file
     */
    File getLogFile() {
        CI.get().file("logs/${name}.log").absoluteFile
    }

    /**
     * Sets the Job Status
     * @param status Job Status
     */
    void setStatus(@NotNull BuildStatus status) {
        this.@status = status
        def jobInfo = CI.get().storage["jobs"][name] as Map<String, ? extends Object>
        jobInfo.status = status.ordinal()
        CI.get().storage.save("jobs")
        CI.get().logger.debug("Job '${name}': Status updated to '${status}'")
    }

    /**
     * Gets the Job Status
     * @return Job Status
     */
    BuildStatus getStatus() {
        return status
    }

    /**
     * Reloads the Build Configuration Script
     */
    void reload() {
        this.buildConfig = JobScript.from(jobFile, this)
    }

    /**
     * Gets the Job History
     * @return job history
     */
    JobHistory getHistory() {
        def history = new JobHistory(this)
        history.load()
        return history
    }

    /**
     * Checks if the job can be built under current build conditions
     * @return true if the build can be built, otherwise false.
     */
    boolean shouldBuild() {
        return !buildConfig.conditions.build.conditions*.call().contains(false)
    }
}
