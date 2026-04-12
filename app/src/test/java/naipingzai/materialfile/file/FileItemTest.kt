/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.file

import com.google.common.truth.Truth.assertThat
import java8.nio.file.Path
import java8.nio.file.attribute.BasicFileAttributes
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.text.Collator

class FileItemTest {

    private fun createFileItem(
        isDirectory: Boolean = false,
        isSymbolicLink: Boolean = false,
        symbolicLinkTargetAttributes: BasicFileAttributes? = null
    ): FileItem {
        val path = mock(Path::class.java)
        `when`(path.toString()).thenReturn("/test/file.txt")
        val attrs = mock(BasicFileAttributes::class.java)
        `when`(attrs.isDirectory).thenReturn(isDirectory)
        `when`(attrs.isSymbolicLink).thenReturn(isSymbolicLink)
        val collationKey = Collator.getInstance().getCollationKey("file.txt")
        return FileItem(
            path = path,
            nameCollationKey = collationKey,
            attributesNoFollowLinks = attrs,
            symbolicLinkTarget = if (isSymbolicLink) "/target" else null,
            symbolicLinkTargetAttributes = symbolicLinkTargetAttributes,
            isHidden = false,
            mimeType = MimeType.TEXT_PLAIN
        )
    }

    @Test
    fun attributes_returnsNoFollowLinks_whenNoSymlink() {
        val item = createFileItem()
        assertThat(item.attributes).isEqualTo(item.attributesNoFollowLinks)
    }

    @Test
    fun attributes_returnsTargetAttributes_whenSymlink() {
        val targetAttrs = mock(BasicFileAttributes::class.java)
        val item = createFileItem(isSymbolicLink = true, symbolicLinkTargetAttributes = targetAttrs)
        assertThat(item.attributes).isEqualTo(targetAttrs)
    }

    @Test
    fun isSymbolicLinkBroken_true_whenTargetNull() {
        val item = createFileItem(isSymbolicLink = true, symbolicLinkTargetAttributes = null)
        assertThat(item.isSymbolicLinkBroken).isTrue()
    }

    @Test
    fun isSymbolicLinkBroken_false_whenTargetPresent() {
        val targetAttrs = mock(BasicFileAttributes::class.java)
        val item = createFileItem(isSymbolicLink = true, symbolicLinkTargetAttributes = targetAttrs)
        assertThat(item.isSymbolicLinkBroken).isFalse()
    }

    @Test(expected = IllegalStateException::class)
    fun isSymbolicLinkBroken_throws_whenNotSymlink() {
        val item = createFileItem(isSymbolicLink = false)
        item.isSymbolicLinkBroken // should throw
    }

    @Test
    fun dataClass_equality() {
        val path = mock(Path::class.java)
        val attrs = mock(BasicFileAttributes::class.java)
        val key = Collator.getInstance().getCollationKey("test")
        val item1 = FileItem(path, key, attrs, null, null, false, MimeType.TEXT_PLAIN)
        val item2 = FileItem(path, key, attrs, null, null, false, MimeType.TEXT_PLAIN)
        assertThat(item1).isEqualTo(item2)
    }

    @Test
    fun mimeType_stored() {
        val item = createFileItem()
        assertThat(item.mimeType).isEqualTo(MimeType.TEXT_PLAIN)
    }

    @Test
    fun isHidden_stored() {
        val path = mock(Path::class.java)
        val attrs = mock(BasicFileAttributes::class.java)
        val key = Collator.getInstance().getCollationKey("test")
        val item = FileItem(path, key, attrs, null, null, true, MimeType.GENERIC)
        assertThat(item.isHidden).isTrue()
    }
}
