package org.directcode.ci.source

import org.jetbrains.annotations.NotNull

/**
 * A Version Control System.
 */
interface VCS {
    VCSChangelog changelog(@NotNull int count)
}