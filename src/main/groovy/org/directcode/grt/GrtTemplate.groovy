package org.directcode.grt

import groovy.text.Template
import groovy.transform.CompileStatic

@CompileStatic
class GrtTemplate {
    private TemplateFactory factory
    private Template template

    GrtTemplate(TemplateFactory factory, Template template) {
        this.factory = factory
        this.template = template
    }

    String make(Map<String, ? extends Object> binding) {
        def realBinding = [
                component: { String name, Map<String, ? extends Object> opts = [:] ->
                    return factory.useComponent(name, opts)
                }
        ] as Map<String, ? extends Object>
        realBinding.putAll(binding)
        def out = template.make(realBinding)
        def writer = new StringWriter()
        out.writeTo(writer)
        return writer.toString()
    }
}
