/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.provider.common

import java8.nio.file.LinkOption

class LinkOptions(val noFollowLinks: Boolean) {
    fun toArray(): Array<LinkOption> {
        val options = mutableListOf<LinkOption>()
        if (noFollowLinks) {
            options += LinkOption.NOFOLLOW_LINKS
        }
        return options.toTypedArray()
    }
}

fun Array<out LinkOption>.toLinkOptions(): LinkOptions {
    var noFollowLinks = false
    for (option in this) {
        when (option) {
            LinkOption.NOFOLLOW_LINKS -> noFollowLinks = true
        }
    }
    return LinkOptions(noFollowLinks)
}
