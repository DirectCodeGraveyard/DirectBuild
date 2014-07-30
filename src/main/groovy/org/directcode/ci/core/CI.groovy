package org.directcode.ci.core

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import org.directcode.ci.api.Source
import org.directcode.ci.api.Task
import org.directcode.ci.build.Build
import org.directcode.ci.build.BuildQueue
import org.directcode.ci.build.BuildStatus
import org.directcode.ci.config.CiConfig
import org.directcode.ci.core.plugins.PluginManager
import org.directcode.ci.jobs.Job
import org.directcode.ci.logging.LogLevel
import org.directcode.ci.logging.Logger
import org.directcode.ci.source.NoneSource
import org.directcode.ci.tasks.CommandTask
import org.directcode.ci.utils.ExecutionTimer
import org.directcode.ci.utils.FileMatcher
import org.directcode.ci.utils.HTTP
import org.directcode.ci.utils.ReleaseInfo
import org.directcode.ci.web.WebServer
import org.jetbrains.annotations.NotNull

@CompileStatic
class CI {

    private static CI INSTANCE

    /**
     * Main CI Logger
     */
    static final Logger logger = Logger.forName("CI")

    /**
     * Configuration Root
     */
    File configRoot

    /**
     * Plugin Manager
     */
    final PluginManager pluginManager

    /**
     * CI Configuration
     */
    final CiConfig config

    /**
     * CI Storage System
     */
    final CIStorage storage

    /**
     * CI Task Types
     */
    final Map<String, Class<? extends Task>> taskTypes = [:]

    /**
     * Source Code Manager Types
     */
    final Map<String, Class<? extends Source>> sourceTypes = [:]

    /**
     * CI Jobs
     */
    final Map<String, Job> jobs = [:]

    /**
     * Job Queue System
     */
    BuildQueue jobQueue

    /**
     * Web Server Management
     */
    final WebServer webServer

    /**
     * CI Event Bus
     */
    final EventBus eventBus

    private CI() {
        configRoot = new File(".").absoluteFile
        config = new CiConfig()
        eventBus = new EventBus()
        webServer = new WebServer()
        storage = new CIStorage()
        pluginManager = new PluginManager()
    }

    /**
     * Starts CI Server
     */
    void start() {
        dispatch("ci.starting")
        init()
        loadJobs()
        Thread.startDaemon { ->
            ResourceExtractor.extractWWW(file("www"))
            dispatch("ci.web.starting")
            logger.debug("Starting Web Server")
            webServer.start(config.webSection().get("port", 8080) as int, config.webSection().get("host", "0.0.0.0") as String)
            dispatch("ci.web.started")
        }
        dispatch("ci.started")
    }

    /**
     * Initializes this CI Server
     */
    private void init() {
        logger.info("Initializing DirectBuild (revision ${ReleaseInfo.releaseID()})")

        dispatch("ci.init.starting")

        def timer = new ExecutionTimer()

        timer.start()

        debuggingSystem()

        dispatch("ci.config.loading")

        config.with {
            configFile = file("config.groovy")
            load()
        }

        dispatch("ci.config.loaded")

        loggingSystem()

        storage.with {
            storagePath = file("storage").absoluteFile.toPath()
            start()
        }

        dispatch("ci.storage.started")

        dispatch("ci.queue.created")
        jobQueue = new BuildQueue(config.ciSection().get("builders", 4) as int)

        file(configRoot, 'logs').absoluteFile.mkdirs()

        loadBuiltins()

        dispatch("ci.plugins.loading")
        pluginManager.loadPlugins()

        dispatch("ci.plugins.loaded")
        dispatch("ci.init", [
                time: System.currentTimeMillis()
        ])

        timer.stop()

        logger.info("Completed Initialization in ${timer.time} milliseconds")
        dispatch("ci.init.completed", [time: timer.time])
    }

    private void loggingSystem() {
        dispatch("ci.logging.setup")
        // Initialize a few loggers
        HTTP.logger
        CrashReporter.logger
        Build.logger
        BuildQueue.logger
        ReleaseInfo.logger


        def logLevel = LogLevel.parse(config.loggingSection().level.toString().toUpperCase())

        Logger.globalLogLevel = logLevel

        def logFile = file("ci.log")

        if (logFile.exists()) {
            logFile.renameTo("ci.log.old")
        }

        Logger.logAllTo(logFile.toPath())
    }

    /**
     * Loads Builtin Tasks and Sources
     */
    private void loadBuiltins() {
        dispatch("ci.builtins.loading")
        registerSource("none", NoneSource)
        registerTask("command", CommandTask)
        dispatch("ci.builtins.loaded")
    }

    /**
     * Loads Jobs from Database and Job Files
     */
    void loadJobs() {
        def jobRoot = file("jobs")

        if (!jobRoot.exists()) {
            dispatch("ci.job.directory.created")
            jobRoot.mkdir()
        }

        dispatch("ci.jobs.loading")

        Map<String, ? extends Object> jobStorage = storage.get("jobs")

        FileMatcher.create(jobRoot).withExtensions(["dbj", "groovy"]) { File file ->
            def job = new Job(file)

            if (jobStorage.containsKey(job.name)) {
                def jobInfo = jobStorage[job.name] as Map<String, ? extends Object>
                job.status = BuildStatus.parse(jobInfo.status as int)
            } else {
                def info = [
                        status: BuildStatus.NOT_STARTED.ordinal()
                ]
                jobStorage[job.name] = info
            }

            jobs[job.name] = job
            dispatch("ci.job.${job.name}.loaded")
        }

        logger.info "Loaded ${jobs.size()} jobs."

        dispatch("ci.jobs.loaded")
    }

    private void debuggingSystem() {
        dispatch("ci.debug.setup")
        on("ci.task.register") { event ->
            logger.debug("Registered task '${event.name}' with type '${(event["type"] as Class<?>).name}'")
        }

        on("ci.source.register") { event ->
            logger.debug("Registered Source '${event.name}' with type '${(event["type"] as Class<?>).name}'")
        }
    }

    /**
     * Adds the Specified Job to the Queue
     * @param job Job to Add to Queue
     * @return A Build that can be used to track status information
     */
    Build runJob(@NotNull Job job) {
        job.shouldBuild() ? jobQueue.add(job) : null
    }

    Build build(@NotNull Job job) {
        runJob(job)
    }

    /**
     * Updates all Jobs from the Database and parses Job Files
     */
    void updateJobs() {
        jobs.values()*.reload()
        dispatch("ci.jobs.reloaded")
    }

    /**
     * Gets where artifacts are stored
     * @return Artifact Directory
     */
    File getArtifactDir() {
        return file("artifacts")
    }

    void registerTask(@NotNull String name, @NotNull Class<? extends Task> taskType, Closure callback = {}) {
        taskTypes[name] = taskType
        dispatch("ci.task.register", [name: name, type: taskType])
        callback()
    }

    void registerSource(@NotNull String name, @NotNull Class<? extends Source> sourceType, Closure callback = {}) {
        sourceTypes[name] = sourceType
        dispatch("ci.source.register", [name: name, type: sourceType])
        callback()
    }

    static CI get() {
        if (INSTANCE == null) {
            INSTANCE = new CI()
        } else {
            return INSTANCE
        }
    }

    void stop() {
        dispatch("ci.shutdown.start")
        webServer.stop()
        jobQueue.stop()
        sleep(500)
        dispatch("ci.shutdown.complete")
        if (INSTANCE == this) {
            INSTANCE = null
        }
    }

    Job getJobByName(String name) {
        return jobs[name]
    }

    Class<? extends Task> getTaskByName(String taskName) {
        return taskTypes[taskName]
    }

    Class<? extends Source> getSourceByName(String sourceName) {
        return sourceTypes[sourceName]
    }

    File file(File parent = configRoot, String path) {
        return new File(parent, path)
    }

    void on(@NotNull String name,
            @NotNull @ClosureParams(value = SimpleType.class, options = "java.util.Map") Closure handler) {
        logger.debug("Registering Event Handler for '${name}'")
        eventBus.on(name, handler)
    }

    void dispatch(@NotNull String eventName, @NotNull Map<String, ? extends Object> options = [:]) {
        logger.debug("Dispatching Event '${eventName}'")
        eventBus.dispatch(eventName, options)
    }
}
