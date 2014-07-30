package org.directcode.ci.web

import org.directcode.ci.core.CI

/**
 * Web Utilities
 */
class WebUtil {
    static CI ci() {
        return CI.get()
    }

    static String logoPath() {
        return "/img/logo.png"
    }

    static String brand() {
        return "DirectBuild"
    }
}
