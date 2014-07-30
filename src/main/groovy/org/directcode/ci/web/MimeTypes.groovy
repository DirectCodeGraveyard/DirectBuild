package org.directcode.ci.web

import groovy.transform.CompileStatic
import groovy.transform.Memoized
import org.directcode.ci.core.CI

/**
 * Mime Type Utilities
 */
@CompileStatic
class MimeTypes {
    static final Map<String, List<String>> types = [
            "font/x-woff"           : [".woff"],
            "text/html"             : [".html", ".htm"],
            "application/json"      : [".json"],
            "application/javascript": [".js"],
            "text/css"              : [".css"],
            "image/*"               : [".png", ".jpeg"]
    ]

    /**
     * Gets a Mime Type for a Filename
     * @param fileName file name
     * @return mime type
     */
    @Memoized(maxCacheSize = 50)
    static String get(String fileName) {
        def extension

        def split = fileName.split("\\.")

        extension = split.size() == 1 ? "" : ".${split.last()}"

        def type = "text/plain"

        types.each { key, value ->
            if (value.findAll {
                it == extension
            }.size() != 0) {
                type = key
            }
        }

        CI.logger.debug("'${fileName}' determined to have the mimetype '${type}'")

        return type
    }
}
