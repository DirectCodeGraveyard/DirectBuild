package org.directcode.ci.utils

import groovy.transform.CompileStatic

@CompileStatic
class ExecuteResult {
    final List<String> output
    final int code

    ExecuteResult(int code, List<String> output) {
        this.code = code
        this.output = output
    }
}
