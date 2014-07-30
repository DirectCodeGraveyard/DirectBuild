package org.directcode.ci.source

import org.directcode.ci.api.Source
import org.directcode.ci.exception.TaskFailedException

class DownloadSource extends Source {

    @Override
    void execute() {
        def urls = [:]

        if (option("urls")) {
            urls.putAll(option("urls") as Map<String, String>)
        } else if (option("url") && option("to")) {
            urls[option("to")] = option("url")
        } else {
            throw new TaskFailedException("The parameters 'urls' or 'url' and 'to' are required")
        }

        urls.each { destination, url ->
            def file = new File(job.buildDir, destination as String)
            file.parentFile.mkdirs()
            log.write("Downloading '${url}' to '${file.absolutePath}'")
            file.bytes = (url as String).toURL().bytes
        }
    }
}
