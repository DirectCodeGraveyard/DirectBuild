package org.directcode.grt

import groovy.text.SimpleTemplateEngine
import groovy.text.TemplateEngine
import groovy.transform.CompileStatic
import groovy.xml.MarkupBuilder

@CompileStatic
class TemplateFactory {
    private final TemplateEngine templateEngine

    final Map<String, Closure> components

    TemplateFactory(TemplateEngine templateEngine) {
        components = [:]
        this.templateEngine = templateEngine
    }

    TemplateFactory() {
        this(new SimpleTemplateEngine())
    }

    GrtTemplate create(Reader reader) {
        return new GrtTemplate(this, templateEngine.createTemplate(reader))
    }

    GrtTemplate create(File file) {
        return new GrtTemplate(this, templateEngine.createTemplate(file))
    }

    void define(String name, @DelegatesTo(Component) Closure closure) {
        components[name] = closure
    }

    protected String useComponent(String name, Map<String, ? extends Object> opts) {
        def component = components[name]
        if (component == null) {
            throw new IllegalArgumentException("Component '${name}' does not exist")
        }
        def c = new Component()
        component.delegate = c
        component(opts)
        def writer = new StringWriter()
        def builder = new MarkupBuilder(new PrintWriter(writer))
        builder.doubleQuotes = true
        builder.expandEmptyElements = true
        def build = c.build
        build.delegate = builder
        build.call(opts)
        return writer.toString()
    }
}
