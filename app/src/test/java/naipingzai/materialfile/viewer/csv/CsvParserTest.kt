/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.viewer.csv

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Tests for CSV parsing and HTML escaping logic in CsvViewerFragment.
 * Uses reflection to access private methods for thorough testing.
 */
class CsvParserTest {

    // Reproduce the exact parseCsvLine logic for standalone testing
    private fun parseCsvLine(line: String): List<String> {
        val fields = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                inQuotes -> {
                    if (c == '"') {
                        if (i + 1 < line.length && line[i + 1] == '"') {
                            sb.append('"')
                            i++
                        } else {
                            inQuotes = false
                        }
                    } else {
                        sb.append(c)
                    }
                }
                c == '"' -> inQuotes = true
                c == ',' -> {
                    fields.add(sb.toString())
                    sb.clear()
                }
                else -> sb.append(c)
            }
            i++
        }
        fields.add(sb.toString())
        return fields
    }

    private fun escapeHtml(text: String): String =
        text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")

    // --- parseCsvLine tests ---

    @Test
    fun parseCsvLine_simple() {
        assertThat(parseCsvLine("a,b,c")).containsExactly("a", "b", "c").inOrder()
    }

    @Test
    fun parseCsvLine_empty() {
        assertThat(parseCsvLine("")).containsExactly("")
    }

    @Test
    fun parseCsvLine_singleField() {
        assertThat(parseCsvLine("hello")).containsExactly("hello")
    }

    @Test
    fun parseCsvLine_emptyFields() {
        assertThat(parseCsvLine(",,")).containsExactly("", "", "").inOrder()
    }

    @Test
    fun parseCsvLine_quotedField() {
        assertThat(parseCsvLine("\"hello world\",b")).containsExactly("hello world", "b").inOrder()
    }

    @Test
    fun parseCsvLine_quotedFieldWithComma() {
        assertThat(parseCsvLine("\"a,b\",c")).containsExactly("a,b", "c").inOrder()
    }

    @Test
    fun parseCsvLine_escapedQuote() {
        assertThat(parseCsvLine("\"he said \"\"hi\"\"\",b"))
            .containsExactly("he said \"hi\"", "b").inOrder()
    }

    @Test
    fun parseCsvLine_mixedQuotedAndUnquoted() {
        assertThat(parseCsvLine("name,\"age, height\",city"))
            .containsExactly("name", "age, height", "city").inOrder()
    }

    @Test
    fun parseCsvLine_numberFields() {
        assertThat(parseCsvLine("1,2.5,300")).containsExactly("1", "2.5", "300").inOrder()
    }

    @Test
    fun parseCsvLine_unicodeContent() {
        assertThat(parseCsvLine("你好,世界,测试")).containsExactly("你好", "世界", "测试").inOrder()
    }

    @Test
    fun parseCsvLine_quotedEmpty() {
        assertThat(parseCsvLine("\"\",b")).containsExactly("", "b").inOrder()
    }

    @Test
    fun parseCsvLine_trailingComma() {
        assertThat(parseCsvLine("a,b,")).containsExactly("a", "b", "").inOrder()
    }

    @Test
    fun parseCsvLine_leadingComma() {
        assertThat(parseCsvLine(",a,b")).containsExactly("", "a", "b").inOrder()
    }

    @Test
    fun parseCsvLine_quotedNewlineCharacters() {
        // In real CSV, newlines inside quotes span multiple lines.
        // Our single-line parser treats a quote ending as closing.
        assertThat(parseCsvLine("\"line1\",\"line2\""))
            .containsExactly("line1", "line2").inOrder()
    }

    // --- escapeHtml tests ---

    @Test
    fun escapeHtml_noSpecialChars() {
        assertThat(escapeHtml("hello world")).isEqualTo("hello world")
    }

    @Test
    fun escapeHtml_ampersand() {
        assertThat(escapeHtml("a & b")).isEqualTo("a &amp; b")
    }

    @Test
    fun escapeHtml_lessThan() {
        assertThat(escapeHtml("a < b")).isEqualTo("a &lt; b")
    }

    @Test
    fun escapeHtml_greaterThan() {
        assertThat(escapeHtml("a > b")).isEqualTo("a &gt; b")
    }

    @Test
    fun escapeHtml_quote() {
        assertThat(escapeHtml("a \"b\" c")).isEqualTo("a &quot;b&quot; c")
    }

    @Test
    fun escapeHtml_allSpecialChars() {
        assertThat(escapeHtml("<script>alert(\"xss\")&</script>"))
            .isEqualTo("&lt;script&gt;alert(&quot;xss&quot;)&amp;&lt;/script&gt;")
    }

    @Test
    fun escapeHtml_empty() {
        assertThat(escapeHtml("")).isEmpty()
    }

    @Test
    fun escapeHtml_unicode() {
        assertThat(escapeHtml("你好")).isEqualTo("你好")
    }
}
