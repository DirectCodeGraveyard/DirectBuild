package org.directcode.ci.source

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

/**
 * A Changelog for VCS Sources
 */
@CompileStatic
class VCSChangelog {
    List<Entry> entries = []

    Entry newEntry() {
        def entry = new Entry()
        entries.add(entry)
        return entry
    }

    void each(@ClosureParams(value = SimpleType, options = ["org.directcode.ci.source.VCSChangelog.Entry"]) Closure closure) {
        entries.each(closure)
    }

    static class Entry {

        String revision
        String message
        String author
    }
}
