package org.directcode.ci.plugins

import org.directcode.ci.api.Plugin
import org.directcode.ci.source.DownloadSource

class DownloadSourcePlugin extends Plugin {
    @Override
    void apply() {
        ci().registerSource("download", DownloadSource)
    }
}
