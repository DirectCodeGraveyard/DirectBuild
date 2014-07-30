package org.directcode.ci.web

import org.vertx.groovy.core.http.HttpServerRequest
import org.vertx.groovy.core.http.RouteMatcher

abstract class DCScript extends Script {
    DataType type
    Closure creator
    RouteMatcher router
    DCScript upper = this

    void type(DataType type) {
        this.type = type
    }

    void create(Closure creator) {
        this.creator = creator
    }

    void mapping(Closure closure) {
        new Mapping()(closure)
    }

    class Mapping {

        void call(Closure closure) {
            closure.delegate = this
            closure()
        }

        void GET(String path) {
            upper.router.get(path) { HttpServerRequest request ->
                upper.type.handle(request, upper.creator)
            }
        }
    }
}
