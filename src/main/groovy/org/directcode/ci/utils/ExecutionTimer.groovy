package org.directcode.ci.utils

import groovy.transform.CompileStatic

import java.util.concurrent.TimeUnit

@CompileStatic
class ExecutionTimer {
    long time
    private long startTime
    private long stopTime

    long start() {
        this.startTime = System.currentTimeMillis()
    }

    long stop() {
        this.stopTime = System.currentTimeMillis()

        this.time = stopTime - startTime
    }

    @Override
    String toString() {
        String.format("%d minutes %d seconds", TimeUnit.MILLISECONDS.toMinutes(time), TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time)))
    }
}
