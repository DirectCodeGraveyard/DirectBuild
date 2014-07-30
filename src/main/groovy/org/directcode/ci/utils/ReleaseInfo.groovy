package org.directcode.ci.utils

import org.directcode.ci.core.CI
import org.directcode.ci.logging.Logger
import org.directcode.ci.web.WebServer

import java.time.LocalDateTime
import java.time.format.TextStyle

class ReleaseInfo {
    static final Logger logger = Logger.forName("Release Info")
    private static ReleaseData data

    /**
     * Loads the Release Information from resource classpath
     */
    static {
        CI.get().on("ci.web.setup") { event ->
            def server = event["web"] as WebServer
            server.get("/api/release") { ->
                request.response.putHeader("Content-Type", "application/json")
                request.response.end(Utils.encodeJSON([
                    releaseID: releaseID(),
                    buildDate: buildDate()
                ]))
            }
        }
        try {
            def loader = CI.class.classLoader
            def releaseStream = loader.getResourceAsStream("release.json")
            data = releaseStream.text.parseJSON(ReleaseData)
        } catch (ignored) {
            logger.error("Failed to loaded release.json - No Release Information Available")
            data = new ReleaseData()
            data.buildDate = "unknown"
            data.GIT_COMMIT_SHA = "unknown"
        }
    }

    /**
     * Git Commit SHA
     * @return Git Commit SHA in full length
     */
    static String gitCommitSHA() {
        def stored = data.GIT_COMMIT_SHA
        if (!stored || stored == '${GIT_COMMIT_SHA}') {
            stored = "unknown"
        }
        stored
    }

    /**
     * The Release ID of this build
     * @return Release ID
     */
    static String releaseID() {
        def sha = gitCommitSHA()
        if (sha.size() > 10) {
            sha = sha[0..9] // First 10 Characters Only
        }
        sha
    }

    static String buildDate() {
        def builtWhen = data.buildDate

        if (builtWhen == "unknown" || builtWhen == '${BUILD_DATE}') {
            return "unknown"
        }

        def when = LocalDateTime.parse(builtWhen)
        def time = when.toLocalTime().toString()
        time = time.substring(0, time.indexOf('.'))
        return "${when.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)} ${when.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)} ${when.dayOfMonth}, ${when.year} at ${time}"
    }

    static class ReleaseData {
        String GIT_COMMIT_SHA
        String buildDate
    }
}
