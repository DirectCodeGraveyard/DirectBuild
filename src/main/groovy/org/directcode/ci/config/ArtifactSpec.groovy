package org.directcode.ci.config

import groovy.transform.CompileStatic

@CompileStatic
class ArtifactSpec {
    final List<String> files = []
    final List<String> directories = []

    /**
     * Adds a File to the Artifacts
     * @param name path to the file
     */
    void file(String name) {
        files.add(name)
    }

    /**
     * Adds a Directory to the Artifacts
     * @param name path to the directory
     */
    void directory(String name) {
        directories.add(name)
    }
}
