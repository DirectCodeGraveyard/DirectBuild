package org.directcode.grt

import groovy.transform.CompileStatic
import groovy.xml.MarkupBuilder

@CompileStatic
class Component {
    protected Closure build

    void build(@DelegatesTo(MarkupBuilder) Closure closure) {
        build = closure
    }
}
