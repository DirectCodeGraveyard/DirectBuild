package org.directcode.ci.api

import org.directcode.ci.core.CI

/**
 * A CI Plugin
 */
abstract class Plugin {
    abstract void apply();

    static CI ci() {
        CI.get()
    }
}
