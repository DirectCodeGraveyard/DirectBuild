package org.directcode.ci.build

import groovy.transform.CompileStatic
import org.directcode.ci.core.CI
import org.directcode.ci.jobs.Job
import org.directcode.ci.logging.Logger
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@CompileStatic
class BuildQueue {
    static final Logger logger = Logger.forName("BuildQueue")
    private final Set<Builder> builders
    private final Map<String, Integer> numbers

    /**
     * Creates a BuildQueue with the specified amount of builders
     * @param builderCount Builder Count
     */
    BuildQueue(@NotNull int builderCount) {
        this.builders = new TreeSet<>()
        (1..builderCount).each { id ->
            def builder = new Builder(id as int)
            builders.add(builder)
            builder.start()
            sleep(1)
        }
        this.numbers = CI.get().storage.get("build_numbers") as Map<String, Integer>
        addShutdownHook(this.&stop)
        CI.get().on("ci.shutdown.start") { event ->
            stop()
        }
    }

    /**
     * Checks if a particular Job is building
     * @param job Job
     * @param exclude a builder to exclude, can be null
     * @return true if the job is building, otherwise false.
     */
    boolean isBuilding(@NotNull Job job, @Nullable Builder exclude = null) {
        for (builder in builders) {
            if (!builder.busy || (exclude != null && builder.is(exclude))) {
                continue
            }
            if (builder.current().job.name == job.name) {
                return true
            }
        }
        return false
    }

    synchronized Build add(@NotNull Job job) {
        def number = numbers.get(job.name, 0) + 1
        numbers[job.name] = number
        def build = new Build(job, number)
        def available = builders.findAll { builder ->
            !builder.busy
        }
        if (!available) {
            builders.first().queue().add(build)
        } else {
            available.first().queue().add(build)
        }
        return build
    }

    /**
     * Gets the number of free builders
     * @return number of free builders
     */
    int freeBuilders() {
        (builders*.free).findAll().size()
    }

    /**
     * Gets the number of Working builders
     * @return number of working builders
     */
    int workingBuilders() {
        (builders*.busy).findAll().size()
    }

    /**
     * Gets the total number of builders
     * @return total number of builders
     */
    int totalBuilders() {
        builders.size()
    }

    /**
     * Combines all the Builder Queues
     * @return All Builds Queued
     */
    Set<Build> buildQueues() {
        def all = [].toSet()
        builders.each {
            all.addAll(it.queue())
        }
        all
    }

    void stop() {
        builders.each { builder ->
            builder.shouldRun = false
            sleep(1)
        }
    }
}
