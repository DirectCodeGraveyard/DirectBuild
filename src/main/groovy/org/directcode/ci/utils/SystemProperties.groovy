package org.directcode.ci.utils

class SystemProperties {
    static String get(String key) {
        return System.getProperty(key)
    }

    static boolean getBoolean(String key) {
        return get(key) as boolean
    }
}
