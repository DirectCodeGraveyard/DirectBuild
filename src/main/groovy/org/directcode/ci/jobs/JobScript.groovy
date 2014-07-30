package org.directcode.ci.jobs

import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilerConfiguration
import org.directcode.ci.config.ArtifactSpec
import org.directcode.ci.config.TaskConfiguration
import org.directcode.ci.core.CI
import org.directcode.ci.exception.JobConfigurationException

@CompileStatic
abstract class JobScript extends Script {

    private static final CompilerConfiguration compilerConfig = {
        def config = new CompilerConfiguration()
        config.scriptBaseClass = JobScript.class.name
        config
    }()

    protected Job job

    String name = this.class.simpleName
    List<TaskConfiguration> tasks = []
    ArtifactSpec artifacts = new ArtifactSpec()
    Map<String, ? extends Object> source = [type: "none"]
    Conditions conditions = new Conditions()


    void name(String name) {
        this.name = name
    }

    void conditions(@DelegatesTo(Conditions) Closure closure) {
        closure.delegate = conditions
        closure()
    }

    void task(String type, Closure closure) {
        if (type in CI.get().taskTypes.keySet()) {
            def config = new TaskConfiguration()
            config.type = type
            config.config = closure
            tasks.add(config)
        } else {
            throw new JobConfigurationException("Task of type '${type}' does not exist!")
        }
    }

    void tasks(@DelegatesTo(TaskContainer) Closure closure) {
        new TaskContainer().with(closure)
    }

    void source(Map<String, Object> opts) {
        source.putAll(opts)
    }

    void artifact(String location) {
        artifacts.file(location)
    }

    void artifacts(@DelegatesTo(ArtifactSpec) Closure closure) {
        artifacts.with(closure)
    }

    void webhooks(@DelegatesTo(WebHooks) Closure closure) {
        closure.delegate = job.webHooks
        closure()
    }

    class TaskContainer {
        @Override
        Object invokeMethod(String name, Object args) {
            def argz = args as Object[]
            def firstArg = argz[0] as Closure

            task(name, firstArg)

            return null
        }
    }

    static JobScript from(File file, Job job) {

        if (!file.exists()) {
            throw new JobConfigurationException("No Such Job Configuration File: ${file.absolutePath}")
        }

        def shell = new GroovyShell(compilerConfig)
        def script = shell.parse(file) as JobScript
        script.job = job
        script.run()
        return script
    }

    class Conditions {

        Condition build = new Condition()

        class Condition {
            List<Closure<Boolean>> conditions = []

            void onlyIf(Closure<Boolean> condition) {
                conditions.add(condition)
            }
        }
    }
}