package org.directcode.ci.web

import groovy.json.JsonBuilder
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration
import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import org.codehaus.groovy.control.CompilerConfiguration
import org.directcode.ci.core.CI
import org.directcode.ci.utils.Utils
import org.directcode.grt.TemplateFactory
import org.vertx.groovy.core.Vertx
import org.vertx.groovy.core.buffer.Buffer
import org.vertx.groovy.core.http.HttpServer
import org.vertx.groovy.core.http.HttpServerRequest
import org.vertx.groovy.core.http.RouteMatcher

import java.nio.file.Path

/**
 * CI Web Server
 */
@CompileStatic
class WebServer {
    final HttpServer server
    final Vertx vertx
    final TemplateFactory templateFactory
    RouteMatcher routeMatcher

    WebServer() {
        vertx = Vertx.newVertx()
        server = vertx.createHttpServer()
        def templateConfig = new TemplateConfiguration()
        templateConfig.autoIndent = true
        templateConfig.autoNewLine = true
        templateConfig.useDoubleQuotes = true
        templateFactory = new TemplateFactory(new MarkupTemplateEngine(templateConfig))
    }

    /**
     * Starts the Web Server
     * @param port server port
     * @param ip server host
     */
    void start(int port, String ip) {
        def matcher = new RouteMatcher()
        routeMatcher = matcher
        configure(matcher)
        server.requestHandler(matcher.asClosure())
        server.listen(port, ip)
    }

    void get(String path, @DelegatesTo(HandlerUtils) @ClosureParams(value = SimpleType, options = "org.vertx.groovy.core.http.HttpServerRequest") Closure handler) {
        routeMatcher.get(path) { request ->
            handler.delegate = new HandlerUtils(request as HttpServerRequest)
            handler()
        }
    }

    private void configure(RouteMatcher matcher) {
        def ci = CI.get()

        BaseComponents.load(templateFactory)

        get("/") {
            template(getStream("index.grt"))
        }

        matcher.get('/css/:file') { HttpServerRequest r ->
            writeResource(r, "css/${r.params['file']}")
        }

        matcher.get('/js/:file') { HttpServerRequest r ->
            writeResource(r, "js/${r.params['file']}")
        }

        matcher.get('/img/:file') { HttpServerRequest r ->
            writeResource(r, "img/${r.params['file']}")
        }

        matcher.get('/fonts/:file') { HttpServerRequest r ->
            writeResource(r, "fonts/${r.params['file']}")
        }

        matcher.get('/job/:name') { HttpServerRequest r ->
            writeTemplate(r, "job.grt")
        }

        matcher.get('/api/log/:job') { HttpServerRequest request ->
            def jobName = request.params['job'] as String

            if (!ci.jobs.containsKey(jobName)) {
                writeTemplate(request, "404.grt")
                return
            }

            def job = ci.jobs[jobName]

            if (!job.logFile.exists()) {
                writeTemplate(request, "404.grt")
            } else {
                request.response.sendFile(job.logFile.absolutePath)
            }
        }

        matcher.get('/hook/:name') { HttpServerRequest it ->
            def jobName = it.params['name'] as String
            it.response.end('')

            if (!ci.jobs.containsKey(jobName)) {
                it.response.end(new JsonBuilder([
                        error: "Job does not exist!"
                ]).toPrettyString())
            }

            it.response.end(new JsonBuilder([
                    error: null,
            ]).toPrettyString())

            def job = ci.jobs[jobName]

            ci.logger.info "Job Hook executing job ${jobName}"

            ci.runJob(job)
        }

        matcher.get('/artifact/:job/:id/:name') { HttpServerRequest request ->
            def jobName = request.params['job'] as String
            def artifact = request.params['name'] as String
            def id = request.params['id'] as String

            if (!ci.jobs.containsKey(jobName)) {
                writeTemplate(request, "404.grt")
                return
            }

            def artifactFile = ci.file("artifacts/${jobName}/${id}/${artifact}")

            if (!artifactFile.exists()) {
                writeTemplate(request, "404.grt")
                return
            }

            request.response.sendFile(artifactFile.absolutePath)
        }

        matcher.get('/jobs') { HttpServerRequest r ->
            writeTemplate(r, "jobs.grt")
        }

        matcher.post('/github/:name') { HttpServerRequest it ->
            def jobName = it.params['name'] as String
            it.response.end('')

            if (!ci.jobs.containsKey(jobName)) return

            def job = ci.jobs[jobName]

            ci.logger.info "GitHub Hook executing job ${jobName}"

            ci.runJob(job)
        }

        matcher.get('/queue') { HttpServerRequest request ->
            writeTemplate(request, "queue.grt")
        }

        matcher.get('/api/history/:name') { HttpServerRequest r ->
            def jobName = r.params['name'] as String

            if (!ci.jobs.containsKey(jobName)) {
                r.response.end(Utils.encodeJSON([
                        code : 404,
                        error: [
                                message: "Job Not Found"
                        ]
                ]) as String)
                return
            }

            def job = ci.jobs[jobName]

            r.response.end(Utils.encodeJSON(job.history.entries))
        }

        matcher.noMatch { HttpServerRequest r ->
            writeTemplate(r, "404.grt")
        }

        ci.eventBus.dispatch("ci.web.setup", [router: matcher, server: server, vertx: vertx, web: this])

        loadDCScripts()
    }

    void loadDCScripts() {
        def scripts = Utils.parseJSON(Utils.resourceToString("dcscripts.json"))
        scripts.each { String scriptPath ->
            loadDCScript(Utils.resource(scriptPath).newReader())
        }
    }

    protected void writeResource(HttpServerRequest r, String path) {
        String mimeType = MimeTypes.get(path)
        InputStream stream = getStream(path)

        r.response.headers.add("Content-Type", mimeType)

        if (stream == null) {
            writeTemplate(r, "404.grt")
            return
        }

        def buffer = new Buffer(stream.bytes)

        r.response.end(buffer)
    }

    protected void writeResource(HttpServerRequest r, Path path) {
        String mimeType = MimeTypes.get(path.toFile().name)
        InputStream stream = path.newInputStream()

        r.response.headers.add("Content-Type", mimeType)

        if (stream == null) {
            writeTemplate(r, "404.grt")
            return
        }

        def buffer = new Buffer(stream.bytes)

        r.response.end(buffer)
    }

    protected void writeTemplate(HttpServerRequest request, String path) {
        request.response.end(templateFactory.create(getStream(path).newReader()).make(ci: CI.get(), request: request))
    }

    protected void writeTemplate(HttpServerRequest request, File path) {
        request.response.end(templateFactory.create(path.newReader()).make(ci: CI.get(), request: request).toString())
    }

    protected void writeTemplate(HttpServerRequest request, InputStream stream) {
        request.response.end(templateFactory.create(stream.newReader()).make(ci: CI.get(), request: request))
    }

    void loadDCScript(Reader reader) {
        def cc = new CompilerConfiguration()
        cc.scriptBaseClass = DCScript.class.name
        def shell = new GroovyShell(cc)
        def script = shell.parse(reader) as DCScript
        script.router = routeMatcher
        script.run()
    }

    private static InputStream getStream(String path) {
        File dir = new File(CI.get().configRoot, "www")
        InputStream stream
        if (!dir.exists()) {
            stream = Utils.resource("www/${path}")
        } else {
            def file = new File(dir, path)
            if (!file.exists()) {
                return null
            }
            stream = file.toPath().newInputStream()
        }
        return stream
    }

    void stop() {
        CI.logger.debug("Stopping Web Server")
        server.close()
    }

    class HandlerUtils {

        final HttpServerRequest request

        HandlerUtils(HttpServerRequest request) {
            this.request = request
        }

        void template(File template) {
            writeTemplate(request, template)
        }

        void template(Path template) {
            writeTemplate(request, template.toFile())
        }

        void file(Path location) {
            writeResource(request, location)
        }

        void file(File location) {
            writeResource(request, location.toPath())
        }

        void template(InputStream stream) {
            writeTemplate(request, stream)
        }
    }
}