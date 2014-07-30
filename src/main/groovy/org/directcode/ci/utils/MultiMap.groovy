package org.directcode.ci.utils

import groovy.transform.CompileStatic

@CompileStatic
class MultiMap<V> {
    private final Map<String, List<V>> delegate

    MultiMap() {
        delegate = [:]
    }

    List<V> getAt(String key) {
        get(key)
    }

    void putAt(String key, V value) {
        add(key, value)
    }

    List<V> get(String key) {
        if (delegate.containsKey(key)) {
            return delegate[key]
        } else {
            return (delegate[key] = [])
        }
    }

    void add(String key, V value) {
        get(key).add(value)
    }

    boolean empty(String key) {
        return !delegate.containsKey(key) || delegate[key].empty
    }

    Set<String> keys() {
        return delegate.keySet()
    }

    List<List<V>> values() {
        return delegate.values().toList()
    }

    void each(String key, Closure closure) {
        get(key).each(closure)
    }
}