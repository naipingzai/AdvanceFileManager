/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.viewer.ebook

import android.util.Log
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.InputStream
import java.io.StringReader
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * EPUB file parser — extracts and merges XHTML content from .epub files.
 *
 * EPUB structure (ZIP archive):
 *   META-INF/container.xml  -> locates the OPF file
 *   content.opf             -> manifest (files) + spine (reading order) + metadata (title)
 *   chapter.xhtml           -> chapter content
 *   style.css               -> stylesheets
 *   images/                 -> embedded images
 */
class EpubParser {

    data class EpubBook(
        val title: String,
        val html: String
    )

    fun parse(inputStream: InputStream, resourceDir: File): EpubBook {
        // Step 1: Read all ZIP entries into memory
        val entries = mutableMapOf<String, ByteArray>()
        ZipInputStream(inputStream).use { zis ->
            var entry: ZipEntry? = zis.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    entries[normalizeZipPath(entry.name)] = zis.readBytes()
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        if (entries.isEmpty()) {
            throw EpubParseException("Empty or invalid EPUB file")
        }

        // Step 2: Find OPF file path from container.xml
        val containerXml = entries["meta-inf/container.xml"]
            ?: throw EpubParseException("Missing META-INF/container.xml")
        val opfPath = parseContainerXml(containerXml.decodeToString())
        val opfData = entries[opfPath.lowercase()]
            ?: throw EpubParseException("OPF file not found: $opfPath")

        // OPF directory is the base for relative paths in manifest
        val opfDir = opfPath.substringBeforeLast('/', "")

        // Step 3: Parse OPF
        val opf = parseOpf(opfData.decodeToString())

        // Step 4: Extract images and CSS to resourceDir
        resourceDir.mkdirs()
        val resourceMap = mutableMapOf<String, String>() // original href -> local filename

        for ((id, item) in opf.manifest) {
            val mediaType = item.mediaType.lowercase()
            if (mediaType.startsWith("image/") || mediaType == "text/css" ||
                mediaType == "font/ttf" || mediaType == "font/otf" ||
                mediaType == "font/woff" || mediaType == "font/woff2" ||
                mediaType == "application/font-sfnt" ||
                mediaType == "application/vnd.ms-opentype"
            ) {
                val href = item.href
                val fullPath = resolveHref(opfDir, href).lowercase()
                val data = entries[fullPath]
                if (data != null) {
                    // Use a flat filename to avoid directory issues
                    val safeFileName = href.replace('/', '_').replace('\\', '_')
                    val outFile = File(resourceDir, safeFileName)
                    outFile.writeBytes(data)
                    resourceMap[href] = safeFileName
                    // Also map by full path
                    resourceMap[fullPath] = safeFileName
                }
            }
        }
        // Step 5: Build combined HTML from spine order
        val htmlBuilder = StringBuilder()
        htmlBuilder.append("<!DOCTYPE html>\n<html>\n<head>\n<meta charset=\"UTF-8\">\n")
        htmlBuilder.append("<title>").append(escapeHtml(opf.title)).append("</title>\n")

        // Inline CSS references
        for ((_, item) in opf.manifest) {
            if (item.mediaType.lowercase() == "text/css") {
                val localName = resourceMap[item.href]
                if (localName != null) {
                    val cssData = File(resourceDir, localName).readText()
                    htmlBuilder.append("<style>\n").append(cssData).append("\n</style>\n")
                }
            }
        }
        htmlBuilder.append("</head>\n<body>\n")

        for (idref in opf.spine) {
            val item = opf.manifest[idref] ?: continue
            if (!item.mediaType.lowercase().let {
                    it.contains("html") || it.contains("xml")
                }) continue

            val href = item.href
            val fullPath = resolveHref(opfDir, href).lowercase()
            val chapterData = entries[fullPath] ?: continue
            val chapterHtml = chapterData.decodeToString()

            // Extract <body> content from chapter XHTML
            val bodyContent = extractBody(chapterHtml)

            // Replace resource references in body content
            val processed = replaceResourceReferences(bodyContent, href, opfDir, resourceMap)

            htmlBuilder.append("<div class=\"epub-chapter\">\n")
            htmlBuilder.append(processed)
            htmlBuilder.append("\n</div>\n<hr/>\n")
        }

        htmlBuilder.append("</body>\n</html>")

        return EpubBook(
            title = opf.title.ifBlank { "Untitled" },
            html = htmlBuilder.toString()
        )
    }

    // ────── container.xml parsing ──────

    private fun parseContainerXml(xml: String): String {
        try {
            val parser = createXmlParser(xml)
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "rootfile") {
                    val path = parser.getAttributeValue(null, "full-path")
                    if (!path.isNullOrBlank()) {
                        return path
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.e("EpubParser", "Error parsing container.xml", e)
        }
        throw EpubParseException("Could not find rootfile in container.xml")
    }

    // ────── OPF parsing ──────

    data class ManifestItem(
        val id: String,
        val href: String,
        val mediaType: String
    )

    data class OpfData(
        val title: String,
        val manifest: Map<String, ManifestItem>,
        val spine: List<String>
    )

    private fun parseOpf(xml: String): OpfData {
        val manifest = mutableMapOf<String, ManifestItem>()
        val spine = mutableListOf<String>()
        var title = ""
        var inTitle = false

        try {
            val parser = createXmlParser(xml)
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        val tag = parser.name?.lowercase() ?: ""
                        when (tag) {
                            "dc:title", "title" -> {
                                // Only use first title
                                if (title.isEmpty()) inTitle = true
                            }
                            "item" -> {
                                val id = parser.getAttributeValue(null, "id") ?: ""
                                val href = parser.getAttributeValue(null, "href") ?: ""
                                val mediaType =
                                    parser.getAttributeValue(null, "media-type") ?: ""
                                if (id.isNotBlank() && href.isNotBlank()) {
                                    manifest[id] = ManifestItem(
                                        id = id,
                                        href = decodeHref(href),
                                        mediaType = mediaType
                                    )
                                }
                            }
                            "itemref" -> {
                                val idref = parser.getAttributeValue(null, "idref") ?: ""
                                if (idref.isNotBlank()) {
                                    spine.add(idref)
                                }
                            }
                        }
                    }
                    XmlPullParser.TEXT -> {
                        if (inTitle) {
                            title = parser.text?.trim() ?: ""
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        val tag = parser.name?.lowercase() ?: ""
                        if (tag == "dc:title" || tag == "title") {
                            inTitle = false
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.e("EpubParser", "Error parsing OPF", e)
        }

        if (spine.isEmpty()) {
            // Fallback: use all HTML items from manifest in order
            for ((id, item) in manifest) {
                if (item.mediaType.lowercase().let { it.contains("html") || it.contains("xml") }
                    && !item.href.lowercase().endsWith("toc.ncx")
                    && !item.href.lowercase().endsWith("toc.xhtml")
                ) {
                    spine.add(id)
                }
            }
        }

        return OpfData(title = title, manifest = manifest, spine = spine)
    }

    // ────── HTML body extraction ──────

    /**
     * Extract content between <body> and </body> tags.
     * Falls back to full content if no body tags found.
     */
    private fun extractBody(xhtml: String): String {
        val bodyStart = xhtml.indexOf("<body", ignoreCase = true)
        if (bodyStart == -1) return xhtml

        val bodyTagEnd = xhtml.indexOf('>', bodyStart)
        if (bodyTagEnd == -1) return xhtml

        val bodyEnd = xhtml.indexOf("</body>", bodyTagEnd, ignoreCase = true)
        return if (bodyEnd != -1) {
            xhtml.substring(bodyTagEnd + 1, bodyEnd)
        } else {
            xhtml.substring(bodyTagEnd + 1)
        }
    }

    // ────── Resource reference replacement ──────

    /**
     * Replace relative paths in chapter content (src="..." href="...") with
     * local resource filenames that have been extracted to the resource directory.
     */
    private fun replaceResourceReferences(
        html: String,
        chapterHref: String,
        opfDir: String,
        resourceMap: Map<String, String>
    ): String {
        // Chapter's directory relative to OPF dir
        val chapterFullPath = resolveHref(opfDir, chapterHref)
        val chapterDir = chapterFullPath.substringBeforeLast('/', "")

        // Replace src="..." and href="..." attributes (not starting with http)
        val attrPattern = Regex(
            """((?:src|href|xlink:href)\s*=\s*["'])([^"'#]+)(#[^"']*)?(['"])""",
            RegexOption.IGNORE_CASE
        )
        return attrPattern.replace(html) { match ->
            val prefix = match.groupValues[1]
            val rawRef = match.groupValues[2]
            val fragment = match.groupValues[3]
            val quote = match.groupValues[4]

            // Skip external URLs
            if (rawRef.startsWith("http://") || rawRef.startsWith("https://") ||
                rawRef.startsWith("data:")
            ) {
                return@replace match.value
            }

            val decoded = decodeHref(rawRef)
            // Resolve relative to chapter's directory
            val resolvedPath = resolvePath(chapterDir, decoded).lowercase()

            // Try to find in resource map
            val localName = resourceMap[decoded]
                ?: resourceMap[resolvedPath]
                ?: resourceMap[rawRef]

            if (localName != null) {
                "$prefix$localName$fragment$quote"
            } else {
                match.value
            }
        }
    }

    // ────── Utility functions ──────

    private fun createXmlParser(xml: String): XmlPullParser {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = false
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xml))
        return parser
    }

    private fun normalizeZipPath(path: String): String =
        path.trimStart('/').replace('\\', '/').lowercase()

    private fun resolveHref(baseDir: String, href: String): String {
        if (baseDir.isEmpty()) return href
        return "$baseDir/$href"
    }

    /**
     * Resolve a relative path against a base directory, handling "../" segments.
     */
    private fun resolvePath(baseDir: String, relativePath: String): String {
        if (relativePath.startsWith("/")) return relativePath.trimStart('/')

        val baseParts = if (baseDir.isEmpty()) {
            mutableListOf()
        } else {
            baseDir.split('/').toMutableList()
        }
        val relParts = relativePath.split('/')

        for (part in relParts) {
            when (part) {
                "", "." -> {} // skip
                ".." -> {
                    if (baseParts.isNotEmpty()) baseParts.removeAt(baseParts.size - 1)
                }
                else -> baseParts.add(part)
            }
        }
        return baseParts.joinToString("/")
    }

    private fun decodeHref(href: String): String =
        try {
            java.net.URLDecoder.decode(href, "UTF-8")
        } catch (e: Exception) {
            href
        }

    private fun escapeHtml(text: String): String =
        text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
}

class EpubParseException(message: String) : Exception(message)
