ruleset {
    /* Basic Rules */
    ruleset("rulesets/basic.xml")
    /* Generic Rules */
    ruleset("rulesets/generic.xml")
    /* Imports Rules */
    ruleset("rulesets/imports.xml")
    /* Groovy-ism Rules */
    ruleset("rulesets/groovyism.xml")
    /* Concurrency Rules */
    ruleset("rulesets/concurrency.xml")
    /* Design Rules */
    ruleset("rulesets/design.xml") {
        exclude 'BuilderMethodWithSideEffects'
        exclude 'AbstractClassWithoutAbstractMethod'
    }
    /* DRY (Don't Repeat Yourself) Rules */
    ruleset("rulesets/dry.xml") {
        exclude 'DuplicateStringLiteral'
    }
}