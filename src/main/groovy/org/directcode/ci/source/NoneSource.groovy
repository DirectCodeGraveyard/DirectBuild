package org.directcode.ci.source

import groovy.transform.CompileStatic
import org.directcode.ci.api.Source

@CompileStatic
class NoneSource extends Source {

    @Override
    void execute() {
        job.buildDir.with {
            deleteDir()
            mkdirs()
        }

        if (option("call")) {
            def toCall = option("call") as Closure

            toCall.delegate = this
            toCall()
        }
    }
}
