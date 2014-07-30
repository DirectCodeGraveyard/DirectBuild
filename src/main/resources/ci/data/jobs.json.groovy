import groovy.json.JsonBuilder
import org.directcode.ci.core.CI
import org.directcode.ci.jobs.Job
import org.directcode.ci.web.DataType
import org.vertx.groovy.core.http.HttpServerRequest

type(DataType.JSON)

create({ HttpServerRequest request, JsonBuilder builder ->
    def info = []
    CI.get().jobs.values().each { Job job ->
        info.add([
                name  : job.name,
                status: job.status.ordinal()
        ])
    }
    builder(info)
})

mapping({
    GET("/api/jobs.json")
})