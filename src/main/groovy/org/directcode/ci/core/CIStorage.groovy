package org.directcode.ci.core

import groovy.io.FileType
import groovy.json.JsonOutput
import groovy.transform.CompileStatic
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

import java.nio.file.Path

/**
 * Storage System for DirectBuild
 */
@CompileStatic
class CIStorage {
    private final Map<String, Map<String, ? extends Object>> storages = [:]
    private Path storagePath
    private final boolean autoSave = true
    private final EventBus eventBus = new EventBus()

    /**
     * Loads all the Storages in the Storage Path
     */
    void load() {
        storagePath.eachFileRecurse(FileType.FILES) { Path path ->
            def file = path.toFile()
            if (!file.name.endsWith(".json")) {
                return
            }
            load(file.name[0..file.name.lastIndexOf('.') - 1])
        }
    }

    /**
     * Loads the Specified Storage
     * @param storageName storage name to load
     */
    synchronized void load(@NotNull String storageName) {
        def path = new File(storagePath.toFile(), "${storageName}.json").toPath()
        storages[storageName] = path.parseJSON() as Map<String, ? extends Object>
    }

    /**
     * Starts the Storage System
     * Internal Use Only.
     */
    protected void start() {
        addShutdownHook { ->
            save()
        }
        Thread.startDaemon { ->
            while (autoSave) {
                save()
                sleep(4000)
            }
        }
    }

    /**
     * Saves all Storages
     */
    void save() {
        storages.keySet().each { String storageName ->
            save(storageName)
        }
        eventBus.dispatch("all.saved")
    }

    /**
     * Saves the Specified Storage
     * @param storageName storage name
     */
    synchronized void save(@NotNull String storageName) {
        def storageFile = new File(storagePath.toFile(), "${storageName}.json")
        def storageOut = storageFile.toPath()
        if (storageFile.exists()) {
            storageFile.delete()
        }
        storageOut.write(JsonOutput.prettyPrint(get(storageName).encodeJSON()))
        eventBus.dispatch("${storageName}.saved")
    }

    /**
     * Sets where to store the storage data
     * @param path path to store data at.
     */
    void setStoragePath(@NotNull Path path) {
        this.storagePath = path
        path.toFile().mkdirs()
    }

    /**
     * Gets the Storage Path
     * @return storage path
     */
    Path getStoragePath() {
        return storagePath
    }

    /**
     * Gets a Storage
     * @param storageName storage name
     * @return the storage data
     */
    Map<String, ? extends Object> get(@NotNull String storageName) {
        if (!(storageName in storages.keySet())) {
            if (new File(storagePath.toFile(), "${storageName}.json").exists()) {
                load(storageName)
            } else {
                storages[storageName] = [:]
            }
        }
        return storages[storageName]
    }

    Map<String, Map<String, ? extends Object>> all() {
        storages
    }

    /**
     * Retrieves the JSON Data of a Storage
     * @param storageName storage name
     * @return data of the storage, if it exists, else null.
     */
    String getJSON(@NotNull String storageName) {
        def file = new File(storagePath.toFile(), "${storageName}.json")
        return !file.exists() ? null : file.text
    }

    /**
     * Registers an Event Handler for when storages are saved
     * @param storageName storage name, if required, if null is specified, uses 'all'
     * @param closure action to call
     */
    void whenSaved(@Nullable String storageName = null, Closure closure) {
        eventBus.on("${storageName ?: "all"}.saved", closure)
    }

    Map<String, ? extends Object> getAt(@NotNull String key) {
        return get(key)
    }
}
