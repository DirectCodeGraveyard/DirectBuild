package org.directcode.ci.jobs

import groovy.io.FileType
import groovy.transform.CompileStatic
import groovy.transform.ToString
import org.directcode.ci.core.CI
import org.directcode.ci.utils.FileMatcher

/**
 * Build History of a Job
 */
@CompileStatic
class JobHistory {
    private final List<Entry> entries = []
    private final Job job

    /**
     * Creates a Job History Object.
     * <p>NOTE: The History is not loaded until the load() method is called</p>
     * @param job job
     */
    JobHistory(Job job) {
        this.job = job
    }

    /**
     * Loads the Job History from the CI Storage System
     */
    void load() {
        def history = ((List<Map<String, ? extends Object>>) CI.get().storage.get("job_history").get(job.name, []))
        CI.get().dispatch("ci.history.load", [job: job, loaded: history, history: this])
        history.each { result ->
            def entry = new Entry()
            entries.add(entry)
            entry.number = result.number as int
            entry.log = result.log as String
            entry.when = result.timeStamp as String
            entry.status = result.status as int
            entry.buildTime = result.buildTime as long
            def artifactDir = CI.get().file("artifacts/${job.name}/${entry.number}")
            artifactDir.mkdirs()
            def files = FileMatcher.create(artifactDir).recursive(FileType.FILES)
            files.each { file ->
                def a = new Artifact()
                a.name = file.absolutePath.replace(artifactDir.absolutePath + "/", "")
                entry.artifacts.add(a)
            }
        }
    }

    /**
     * Gets the Job History Entries
     * @return History Entries
     */
    List<Entry> getEntries() {
        return entries
    }

    /**
     * Retrieves the Latest Build from the History
     * @return latest build
     */
    Entry getLatestBuild() {
        entries.empty ? null : entries.last()
    }

    @Override
    String toString() {
        entries.join("\n")
    }

    /**
     * A Job History Entry
     */
    @ToString
    @CompileStatic
    static class Entry {
        /**
         * Build Status ID
         */
        int status

        /**
         * Build Number
         */
        int number

        /**
         * Build Log in Base64 Format
         */
        String log

        /**
         * Timestamp of the Build in new Date() format.
         */
        String when

        /**
         * Time it took to build
         */
        long buildTime

        /**
         * Artifacts that were the output
         */
        List<Artifact> artifacts = []
    }

    /**
     * A Build Artifact
     */
    static class Artifact {
        /**
         * Name of the Artifact
         */
        String name
    }
}
