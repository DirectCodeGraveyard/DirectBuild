package org.directcode.ci.config

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import org.directcode.ci.api.Task
import org.directcode.ci.core.CI

/**
 * A Task Configuration
 */
@Canonical
@CompileStatic
class TaskConfiguration {
    /**
     * The Task Type
     */
    String type

    /**
     * The Configuration Closure
     */
    Closure config

    /**
     * Creates a Task Instance
     * @return Task Instance
     */
    Task create() {
        return CI.get().taskTypes[type].getConstructor().newInstance()
    }
}
