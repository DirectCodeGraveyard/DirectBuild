package org.directcode.ci.utils

import groovy.transform.CompileStatic
import org.directcode.ci.logging.Logger

@CompileStatic
class HTTP {
    static final Logger logger = Logger.forName("HTTP")

    static void post(Map<String, ? extends Object> options, Closure callback = { void }) {
        logger.debug("Posting to '${options.url}'")
        def url = option(options, "url", true) as String
        def data = option(options, "data", false, [:]) as Map<String, Object>
        def connection = url.toURL().openConnection() as HttpURLConnection
        connection.with {
            requestMethod = "POST"
            logger.debug("Setting Content-Type for '${url}'")
            doOutput = true
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            logger.debug("Connecting to '${options.url}'")
            connect()
            outputStream.withWriter { Writer w ->
                data.entrySet().each { entry ->
                    w.write("${entry.key}=${URLEncoder.encode(entry.value as String, "UTF-8")}".toString())
                }
            }
        }
        logger.debug("Executing Callback for '${url}'")
        callback.call([data: connection.inputStream.text])
        connection.disconnect()
        logger.debug("Disconnected from '${url}'")
    }

    static void get(Map<String, ? extends Object> options, Closure callback = { void }) {
        def url = option(options, "url", true) as String
        logger.debug("Getting '${options.url}'")
        callback(url.toURL().text)
    }

    private
    static Object option(Map<String, ? extends Object> options, String option, boolean required = false, Object defaultValue = null) {
        if (!options.containsKey(option) && required) {
            throw new InvalidObjectException("Option ${option} is not present, but is required!")
        }
        return options.get(option, defaultValue)
    }
}
