package org.directcode.ci.build

import groovy.transform.Canonical

@Canonical
class BuildDescriptor {
    String jobName
    int number
}
