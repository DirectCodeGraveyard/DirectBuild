package org.directcode.ci.exception

import groovy.transform.InheritConstructors

@InheritConstructors
class CIException extends Exception {
    void writeTo(File file) {
        printStackTrace(file.newPrintWriter())
    }
}
