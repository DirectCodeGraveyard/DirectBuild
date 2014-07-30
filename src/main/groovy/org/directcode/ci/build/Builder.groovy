package org.directcode.ci.build

import groovy.transform.CompileStatic
import org.directcode.ci.core.CI

@CompileStatic
class Builder implements Runnable, Comparable<Builder> {
    private final int id
    private final Thread thread = new Thread(this)

    private boolean busy = false

    private final Set<Build> builderQueue = new HashSet<>()

    /**
     * If the Builder should still run.
     * Internal Use Only.
     */
    protected boolean shouldRun = true

    private Build current = null

    /**
     * Creates a new Builder
     * @param id builder id
     */
    Builder(int id) {
        this.id = id
    }

    /**
     * Starts the Builder
     */
    void start() {
        thread.start()
    }

    /**
     * Internal Use Only.
     */
    @Override
    void run() {
        def ci = CI.get()
        ci.dispatch("ci.builder.starting", [id: id, builder: this])
        ci.logger.debug("Builder ${id} starting up.")
        while (shouldRun) {
            while (builderQueue.empty) {
                if (!shouldRun) {
                    onStop()
                    ci.dispatch("ci.builder.stopped", [id: id, builder: this])
                    return
                }
                sleep(2)
            }
            busy = true
            ci.logger.debug("Builder ${id} is busy.")
            def build = builderQueue.first()
            current = build
            builderQueue.remove(build)

            ci.dispatch("ci.build.queued", [
                    jobName: build.job.name,
                    number : build.number,
                    build  : build
            ])

            def count = 0
            while (ci.jobQueue.isBuilding(build.job, this)) {
                count++
                if (count == 100) {
                    count = 0
                    ci.dispatch("ci.build.waiting", [
                            jobName: build.job.name,
                            number : build.number,
                            build  : build
                    ])
                    build.logger.debug("Build '${build.job.name}:${build.number}' waiting for another build to complete.")
                }
                sleep(50)
            }
            build.execute()
            busy = false
            ci.logger.debug("Builder ${id} is no longer busy.")
        }
        onStop()
        ci.dispatch("ci.builder.stopped", [id: id, builder: this])
    }

    private void onStop() {
        CI.logger.debug("Builder ${id} shutting down.")
    }

    /**
     * Checks if the builder is busy
     * @return true if the builder is busy, otherwise false.
     */
    boolean isBusy() {
        return busy
    }

    /**
     * Checks if the Builder is free
     * @return true if the builder is free, otherwise false.
     */
    boolean isFree() {
        return !busy
    }

    /**
     * Retrieves the Builder's Queue
     * @return builder's queue
     */
    Set<Build> queue() {
        return builderQueue
    }

    /**
     * Retrieves the Current Build
     * @return current build, if one exists, otherwise null.
     */
    Build current() {
        return current
    }

    /**
     * Retrieves the Builder's ID
     * @return builder id
     */
    int id() {
        return id
    }

    int compareTo(Builder other) {
        id.compareTo(other.id)
    }
}
