/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.viewer.ebook

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.lang.reflect.Method
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class EpubParserTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var parser: EpubParser

    @Before
    fun setUp() {
        parser = EpubParser()
    }

    // ── Helper to invoke private methods via reflection ──

    private fun invokePrivate(name: String, vararg args: Any?): Any? {
        val method = EpubParser::class.java.declaredMethods.first { it.name == name }
        method.isAccessible = true
        return method.invoke(parser, *args)
    }

    // ── normalizeZipPath tests ──

    @Test
    fun normalizeZipPath_removesLeadingSlash() {
        assertThat(invokePrivate("normalizeZipPath", "/chapter1.xhtml"))
            .isEqualTo("chapter1.xhtml")
    }

    @Test
    fun normalizeZipPath_lowercases() {
        assertThat(invokePrivate("normalizeZipPath", "META-INF/container.xml"))
            .isEqualTo("meta-inf/container.xml")
    }

    @Test
    fun normalizeZipPath_replacesBackslash() {
        assertThat(invokePrivate("normalizeZipPath", "OEBPS\\chapter1.xhtml"))
            .isEqualTo("oebps/chapter1.xhtml")
    }

    @Test
    fun normalizeZipPath_combined() {
        assertThat(invokePrivate("normalizeZipPath", "/OEBPS\\Chapter.XHTML"))
            .isEqualTo("oebps/chapter.xhtml")
    }

    // ── resolveHref tests ──

    @Test
    fun resolveHref_emptyBaseDir() {
        assertThat(invokePrivate("resolveHref", "", "chapter.xhtml"))
            .isEqualTo("chapter.xhtml")
    }

    @Test
    fun resolveHref_withBaseDir() {
        assertThat(invokePrivate("resolveHref", "OEBPS", "chapter.xhtml"))
            .isEqualTo("OEBPS/chapter.xhtml")
    }

    // ── resolvePath tests ──

    @Test
    fun resolvePath_simple() {
        assertThat(invokePrivate("resolvePath", "OEBPS", "chapter.xhtml"))
            .isEqualTo("OEBPS/chapter.xhtml")
    }

    @Test
    fun resolvePath_withDotDot() {
        assertThat(invokePrivate("resolvePath", "OEBPS/Text", "../Images/cover.jpg"))
            .isEqualTo("OEBPS/Images/cover.jpg")
    }

    @Test
    fun resolvePath_absolutePath() {
        assertThat(invokePrivate("resolvePath", "OEBPS", "/absolute.xhtml"))
            .isEqualTo("absolute.xhtml")
    }

    @Test
    fun resolvePath_emptyBaseDir() {
        assertThat(invokePrivate("resolvePath", "", "chapter.xhtml"))
            .isEqualTo("chapter.xhtml")
    }

    @Test
    fun resolvePath_dotSegment() {
        assertThat(invokePrivate("resolvePath", "OEBPS", "./chapter.xhtml"))
            .isEqualTo("OEBPS/chapter.xhtml")
    }

    // ── decodeHref tests ──

    @Test
    fun decodeHref_encoded() {
        assertThat(invokePrivate("decodeHref", "chapter%201.xhtml"))
            .isEqualTo("chapter 1.xhtml")
    }

    @Test
    fun decodeHref_plain() {
        assertThat(invokePrivate("decodeHref", "chapter.xhtml"))
            .isEqualTo("chapter.xhtml")
    }

    // ── escapeHtml tests ──

    @Test
    fun escapeHtml_specialChars() {
        assertThat(invokePrivate("escapeHtml", "<div class=\"test\">&"))
            .isEqualTo("&lt;div class=&quot;test&quot;&gt;&amp;")
    }

    @Test
    fun escapeHtml_plainText() {
        assertThat(invokePrivate("escapeHtml", "Hello World"))
            .isEqualTo("Hello World")
    }

    // ── extractBody tests ──

    @Test
    fun extractBody_withBodyTags() {
        val xhtml = "<html><head></head><body>Hello World</body></html>"
        assertThat(invokePrivate("extractBody", xhtml))
            .isEqualTo("Hello World")
    }

    @Test
    fun extractBody_noBodyTags() {
        val xhtml = "Just plain text"
        assertThat(invokePrivate("extractBody", xhtml))
            .isEqualTo("Just plain text")
    }

    @Test
    fun extractBody_bodyWithAttributes() {
        val xhtml = "<html><body class=\"main\">Content</body></html>"
        assertThat(invokePrivate("extractBody", xhtml))
            .isEqualTo("Content")
    }

    @Test
    fun extractBody_noClosingBody() {
        val xhtml = "<html><body>Content remainder"
        assertThat(invokePrivate("extractBody", xhtml))
            .isEqualTo("Content remainder")
    }

    // ── Full parse test with minimal valid EPUB ──

    @Test
    fun parse_minimalValidEpub() {
        val containerXml = """<?xml version="1.0" encoding="UTF-8"?>
            <container xmlns="urn:oasis:names:tc:opendocument:xmlns:container" version="1.0">
              <rootfiles>
                <rootfile full-path="content.opf" media-type="application/oebps-package+xml"/>
              </rootfiles>
            </container>"""

        val contentOpf = """<?xml version="1.0" encoding="UTF-8"?>
            <package xmlns="http://www.idpf.org/2007/opf" version="3.0">
              <metadata>
                <dc:title>Test Book</dc:title>
              </metadata>
              <manifest>
                <item id="ch1" href="chapter1.xhtml" media-type="application/xhtml+xml"/>
              </manifest>
              <spine>
                <itemref idref="ch1"/>
              </spine>
            </package>"""

        val chapterXhtml = """<?xml version="1.0" encoding="UTF-8"?>
            <html><head><title>Chapter 1</title></head>
            <body><p>Hello, EPUB!</p></body></html>"""

        val epubBytes = createEpubZip(
            "META-INF/container.xml" to containerXml.toByteArray(),
            "content.opf" to contentOpf.toByteArray(),
            "chapter1.xhtml" to chapterXhtml.toByteArray()
        )

        val resourceDir = tempFolder.newFolder("resources")
        val book = parser.parse(ByteArrayInputStream(epubBytes), resourceDir)

        assertThat(book.title).isEqualTo("Test Book")
        assertThat(book.html).contains("<p>Hello, EPUB!</p>")
        assertThat(book.html).contains("<!DOCTYPE html>")
    }

    @Test
    fun parse_emptyZip_throws() {
        val emptyZip = createEpubZip()
        val resourceDir = tempFolder.newFolder("resources2")
        try {
            parser.parse(ByteArrayInputStream(emptyZip), resourceDir)
            assertThat(false).isTrue() // should not reach here
        } catch (e: EpubParseException) {
            assertThat(e.message).contains("Empty or invalid EPUB file")
        }
    }

    @Test
    fun parse_missingContainerXml_throws() {
        val zipBytes = createEpubZip(
            "content.opf" to "dummy".toByteArray()
        )
        val resourceDir = tempFolder.newFolder("resources3")
        try {
            parser.parse(ByteArrayInputStream(zipBytes), resourceDir)
            assertThat(false).isTrue()
        } catch (e: EpubParseException) {
            assertThat(e.message).contains("container.xml")
        }
    }

    @Test
    fun parse_untitledFallback() {
        val containerXml = """<?xml version="1.0"?>
            <container><rootfiles>
              <rootfile full-path="content.opf"/>
            </rootfiles></container>"""
        val contentOpf = """<?xml version="1.0"?>
            <package><manifest>
              <item id="c1" href="c1.xhtml" media-type="application/xhtml+xml"/>
            </manifest><spine><itemref idref="c1"/></spine></package>"""
        val chapter = "<html><body>Text</body></html>"

        val zipBytes = createEpubZip(
            "META-INF/container.xml" to containerXml.toByteArray(),
            "content.opf" to contentOpf.toByteArray(),
            "c1.xhtml" to chapter.toByteArray()
        )
        val resourceDir = tempFolder.newFolder("resources4")
        val book = parser.parse(ByteArrayInputStream(zipBytes), resourceDir)
        assertThat(book.title).isEqualTo("Untitled")
    }

    // ── Helper to create EPUB (ZIP) bytes ──

    private fun createEpubZip(vararg entries: Pair<String, ByteArray>): ByteArray {
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zos ->
            for ((name, data) in entries) {
                zos.putNextEntry(ZipEntry(name))
                zos.write(data)
                zos.closeEntry()
            }
        }
        return baos.toByteArray()
    }
}
