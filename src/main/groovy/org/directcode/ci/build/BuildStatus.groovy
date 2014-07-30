package org.directcode.ci.build

import groovy.transform.CompileStatic
import org.jetbrains.annotations.NotNull

@CompileStatic
enum BuildStatus {
    SUCCESS, FAILURE, NOT_STARTED, RUNNING, WAITING;

    @Override
    String toString() {
        name().toLowerCase().replace('_', ' ').tokenize(' ').collect {
            it.capitalize()
        }.join(' ')
    }

    /**
     * Parses a Build Status ID
     * @param id id
     * @return Build Status
     */
    static BuildStatus parse(@NotNull int id) {
        if (id < 0 || id >= values().size())
            return NOT_STARTED
        return values()[id]
    }

    /**
     * Gets the Bootstrap Context Class
     * @return context class
     */
    String getContextClass() {
        switch (this) {
            case SUCCESS: return "success"
            case FAILURE: return "danger"
            case WAITING: return "warning"
            default: return ""
        }
    }
}